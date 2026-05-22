package org.jeecg.modules.feplatform.template.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.feplatform.application.entity.FeApplication;
import org.jeecg.modules.feplatform.application.service.IFeApplicationService;
import org.jeecg.modules.feplatform.template.client.GitLabClient;
import org.jeecg.modules.feplatform.template.entity.FeAppCreateRecord;
import org.jeecg.modules.feplatform.template.entity.FeTemplate;
import org.jeecg.modules.feplatform.template.entity.FeTemplateVersion;
import org.jeecg.modules.feplatform.template.mapper.FeAppCreateRecordMapper;
import org.jeecg.modules.feplatform.template.service.IFeAppCreateRecordService;
import org.jeecg.modules.feplatform.template.service.IFeTemplateService;
import org.jeecg.modules.feplatform.template.service.IFeTemplateVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class FeAppCreateRecordServiceImpl extends ServiceImpl<FeAppCreateRecordMapper, FeAppCreateRecord>
        implements IFeAppCreateRecordService {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*}}");

    @Autowired
    private IFeTemplateService templateService;

    @Autowired
    private IFeTemplateVersionService templateVersionService;

    @Autowired
    private IFeApplicationService applicationService;

    @Autowired
    private GitLabClient gitLabClient;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public FeAppCreateRecord createAppByWizard(WizardRequest req) {
        if (req == null || req.templateId == null) {
            throw new JeecgBootException("缺少模板ID");
        }
        FeTemplate template = templateService.getById(req.templateId);
        if (template == null) {
            throw new JeecgBootException("模板不存在");
        }
        FeTemplateVersion version = req.versionId != null ? templateVersionService.getById(req.versionId) : null;
        String ref = (version != null && version.getGitTag() != null && !version.getGitTag().isEmpty())
                ? version.getGitTag()
                : (template.getBranch() == null ? "main" : template.getBranch());

        FeAppCreateRecord record = new FeAppCreateRecord();
        record.setTemplateId(template.getId());
        record.setVersionId(version == null ? null : version.getId());
        record.setAppName(req.appName);
        record.setAppCode(req.appCode);
        record.setParams(req.params);
        record.setOutputType(req.outputType);
        record.setStatus("generating");
        record.setCreateTime(new Date());
        save(record);

        try {
            // 1. 下载 archive.zip
            byte[] archive = downloadArchive(template, req, ref);

            // 2. 解压 + 占位变量替换 + 重新打包
            byte[] zipped = replaceAndRepack(archive, req.params);

            // 3. 输出
            String gitlabUrl = null;
            if ("gitlab".equalsIgnoreCase(req.outputType)) {
                gitlabUrl = pushToGitlab(req, zipped);
            } else {
                Path out = saveZipToTmp(req.appCode, zipped);
                gitlabUrl = out.toString();
            }

            // 4. 写入应用主表
            FeApplication app = new FeApplication();
            app.setAppShortName(req.appShortName == null ? req.appName : req.appShortName);
            app.setAppName(req.appName);
            app.setAppCode(req.appCode);
            app.setRepoUrl("gitlab".equalsIgnoreCase(req.outputType) ? gitlabUrl : null);
            app.setRepoBranch(template.getBranch());
            app.setOwnerId(req.ownerId);
            app.setStatus("active");
            app.setCreateTime(new Date());
            applicationService.save(app);

            record.setAppId(app.getId());
            record.setGitlabUrl("gitlab".equalsIgnoreCase(req.outputType) ? gitlabUrl : null);
            record.setStatus("success");
            updateById(record);
            return record;

        } catch (Exception ex) {
            log.error("[Template] wizard failed", ex);
            record.setStatus("failed");
            record.setErrorMessage(ex.getMessage());
            updateById(record);
            throw new JeecgBootException("应用创建失败: " + ex.getMessage());
        }
    }

    /* ============================================================ */

    private byte[] downloadArchive(FeTemplate template, WizardRequest req, String ref) {
        String repoUrl = template.getGitlabUrl();
        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new JeecgBootException("模板未配置 GitLab 仓库地址");
        }
        String gitlabBase = req.gitlabUrl != null ? req.gitlabUrl : extractGitlabBase(repoUrl);
        String token = req.gitlabToken;
        String projectId = gitLabClient.resolveProjectId(gitlabBase, token, repoUrl);
        String archiveUrl = gitLabClient.getArchiveUrl(gitlabBase, token, projectId, ref);

        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("PRIVATE-TOKEN", token);
        }
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                archiveUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        byte[] body = resp.getBody();
        if (body == null) {
            throw new JeecgBootException("下载模板 archive 失败");
        }
        return body;
    }

    private byte[] replaceAndRepack(byte[] archive, Map<String, Object> params) throws Exception {
        Map<String, String> vars = new HashMap<>();
        if (params != null) {
            params.forEach((k, v) -> vars.put(k, v == null ? "" : v.toString()));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archive));
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry;
            byte[] buf = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                ByteArrayOutputStream entryOut = new ByteArrayOutputStream();
                int n;
                while ((n = zis.read(buf)) > 0) {
                    entryOut.write(buf, 0, n);
                }
                byte[] content = entryOut.toByteArray();
                if (isLikelyText(entry.getName())) {
                    String s = new String(content, StandardCharsets.UTF_8);
                    s = renderTemplate(s, vars);
                    content = s.getBytes(StandardCharsets.UTF_8);
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                zos.putNextEntry(copy);
                zos.write(content);
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private String renderTemplate(String text, Map<String, String> vars) {
        Matcher m = VAR_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String val = vars.getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(val));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private boolean isLikelyText(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt") || lower.endsWith(".json")
                || lower.endsWith(".yml") || lower.endsWith(".yaml") || lower.endsWith(".xml")
                || lower.endsWith(".html") || lower.endsWith(".vue") || lower.endsWith(".js")
                || lower.endsWith(".ts") || lower.endsWith(".tsx") || lower.endsWith(".jsx")
                || lower.endsWith(".css") || lower.endsWith(".scss") || lower.endsWith(".less")
                || lower.endsWith(".java") || lower.endsWith(".properties") || lower.endsWith(".env")
                || lower.endsWith(".gitignore") || lower.endsWith(".gitlab-ci.yml");
    }

    private Path saveZipToTmp(String appCode, byte[] zipped) throws Exception {
        String name = (appCode == null ? "app" : appCode) + "-" + System.currentTimeMillis() + ".zip";
        Path out = Files.createTempFile(name, ".zip");
        try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
            fos.write(zipped);
        }
        log.info("[Template] generated zip: {}", out);
        return out;
    }

    private String pushToGitlab(WizardRequest req, byte[] zipped) throws Exception {
        if (req.gitlabUrl == null || req.gitlabToken == null || req.gitlabNamespaceId == null) {
            throw new JeecgBootException("缺少 gitlabUrl/gitlabToken/gitlabNamespaceId 参数");
        }
        // 1. 创建新仓库
        JSONObject project = gitLabClient.createProject(req.gitlabUrl, req.gitlabToken,
                req.appCode, req.gitlabNamespaceId,
                "Generated by FE Platform - " + (req.appName == null ? req.appCode : req.appName));
        String webUrl = project.getString("web_url");
        String defaultBranch = project.getString("default_branch");
        if (defaultBranch == null) {
            defaultBranch = "main";
        }
        String projectIdStr = project.getString("id");

        // 2. 解压并逐个文件 commit（小型模板可接受；大型可改用 git push）
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipped))) {
            ZipEntry entry;
            byte[] buf = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                ByteArrayOutputStream entryOut = new ByteArrayOutputStream();
                int n;
                while ((n = zis.read(buf)) > 0) {
                    entryOut.write(buf, 0, n);
                }
                // gitlab archive 顶层是 <project>-<sha>/...，去掉首段
                String name = stripFirstSegment(entry.getName());
                if (name.isEmpty() || "README.md".equalsIgnoreCase(name)) continue;
                String text = new String(entryOut.toByteArray(), StandardCharsets.UTF_8);
                try {
                    gitLabClient.createFile(req.gitlabUrl, req.gitlabToken, projectIdStr,
                            name, defaultBranch, text,
                            "init: bootstrap from template", null, null);
                } catch (Exception ex) {
                    log.warn("[Template] commit file {} failed: {}", name, ex.getMessage());
                }
            }
        }
        return webUrl;
    }

    private String stripFirstSegment(String path) {
        int idx = path.indexOf('/');
        return idx < 0 ? "" : path.substring(idx + 1);
    }

    private String extractGitlabBase(String repoUrl) {
        // http://host/group/project.git → http://host
        int schemeEnd = repoUrl.indexOf("://");
        if (schemeEnd < 0) return repoUrl;
        int slash = repoUrl.indexOf('/', schemeEnd + 3);
        return slash < 0 ? repoUrl : repoUrl.substring(0, slash);
    }
}
