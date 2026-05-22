package org.jeecg.modules.feplatform.template.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * GitLab REST API v4 客户端
 * <p>
 * 仅依赖项目已有的 RestTemplate，使用 Personal Access Token 认证。
 * </p>
 *
 * @author feplatform
 */
@Slf4j
@Component
public class GitLabClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 列出指定仓库的分支。
     */
    public JSONArray listBranches(String gitlabUrl, String token, String projectId) {
        String url = buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/repository/branches";
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        return JSONArray.parseArray(resp.getBody());
    }

    /**
     * 列出指定仓库的标签。
     */
    public JSONArray listTags(String gitlabUrl, String token, String projectId) {
        String url = buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/repository/tags";
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        return JSONArray.parseArray(resp.getBody());
    }

    /**
     * 获取仓库指定路径的文件内容（返回 base64 内容）。
     */
    public JSONObject getFile(String gitlabUrl, String token, String projectId,
                              String filePath, String ref) {
        String url = UriComponentsBuilder.fromUriString(
                buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/repository/files/" + encode(filePath))
                .queryParam("ref", ref)
                .toUriString();
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        return JSONObject.parseObject(resp.getBody());
    }

    /**
     * 创建项目（在指定 group 下）。
     * <p>
     * POST /api/v4/projects  body: { name, namespace_id, ... }
     * </p>
     */
    public JSONObject createProject(String gitlabUrl, String token, String name,
                                     Integer namespaceId, String description) {
        String url = buildApi(gitlabUrl) + "/projects";
        JSONObject body = new JSONObject();
        body.put("name", name);
        if (namespaceId != null) {
            body.put("namespace_id", namespaceId);
        }
        if (description != null) {
            body.put("description", description);
        }
        body.put("initialize_with_readme", true);
        HttpHeaders headers = tokenHeader(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body.toJSONString(), headers), String.class);
        return JSONObject.parseObject(resp.getBody());
    }

    /**
     * 根据仓库 URL 解析项目 ID (path_with_namespace → URL encoded)。
     * GitLab API v4 支持用 URL 编码的 path 代替数字 ID。
     * <p>
     * 例: http://gitlab.local/group/project → "group%2Fproject"
     * </p>
     */
    public String resolveProjectId(String gitlabUrl, String token, String repoUrl) {
        // repoUrl: http://host/group/project.git → group/project
        String path = repoToPath(repoUrl);
        String url = buildApi(gitlabUrl) + "/projects/" + encode(path);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        JSONObject obj = JSONObject.parseObject(resp.getBody());
        return obj == null ? encode(path) : obj.getString("id");
    }

    /**
     * 为仓库注册 Webhook。
     */
    public JSONObject addWebhook(String gitlabUrl, String token, String projectId,
                                  String webhookUrl, String pushEvents, String mergeRequestEvents) {
        String url = buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/hooks";
        JSONObject body = new JSONObject();
        body.put("url", webhookUrl);
        body.put("push_events", "true".equals(pushEvents));
        body.put("merge_requests_events", "true".equals(mergeRequestEvents));
        body.put("tag_push_events", true);
        HttpHeaders headers = tokenHeader(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body.toJSONString(), headers), String.class);
        return JSONObject.parseObject(resp.getBody());
    }

    /**
     * 列出仓库已有的 Webhooks。
     */
    public JSONArray listWebhooks(String gitlabUrl, String token, String projectId) {
        String url = buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/hooks";
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        return JSONArray.parseArray(resp.getBody());
    }

    /**
     * 获取仓库 Archive 下载链接（zip）。
     */
    public String getArchiveUrl(String gitlabUrl, String token, String projectId, String ref) {
        return buildApi(gitlabUrl) + "/projects/" + encode(projectId)
                + "/repository/archive.zip?sha=" + ref + "&private_token=" + token;
    }

    /**
     * 获取指定分支的最新 commit。
     */
    public JSONObject getLatestCommit(String gitlabUrl, String token, String projectId, String ref) {
        String url = UriComponentsBuilder.fromUriString(
                buildApi(gitlabUrl) + "/projects/" + encode(projectId) + "/repository/commits")
                .queryParam("ref_name", ref)
                .queryParam("per_page", 1)
                .toUriString();
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                entity(token), String.class);
        JSONArray arr = JSONArray.parseArray(resp.getBody());
        return (arr != null && !arr.isEmpty()) ? arr.getJSONObject(0) : null;
    }

    /**
     * 向仓库提交/创建文件。
     */
    public JSONObject createFile(String gitlabUrl, String token, String projectId,
                                  String filePath, String branch, String content,
                                  String commitMessage, String authorName, String authorEmail) {
        String url = buildApi(gitlabUrl) + "/projects/" + encode(projectId)
                + "/repository/files/" + encode(filePath);
        JSONObject body = new JSONObject();
        body.put("branch", branch);
        body.put("content", content);
        body.put("commit_message", commitMessage);
        body.put("encoding", "text");
        if (authorName != null) {
            body.put("author_name", authorName);
        }
        if (authorEmail != null) {
            body.put("author_email", authorEmail);
        }
        HttpHeaders headers = tokenHeader(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(body.toJSONString(), headers), String.class);
        return JSONObject.parseObject(resp.getBody());
    }

    /**
     * 连通性测试。
     */
    public Map<String, Object> ping(String gitlabUrl, String token) {
        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        try {
            String url = buildApi(gitlabUrl) + "/version";
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                    entity(token), String.class);
            result.put("success", resp.getStatusCode().is2xxSuccessful());
            result.put("body", resp.getBody());
        } catch (Exception ex) {
            log.warn("GitLab ping failed: {}", ex.getMessage());
            result.put("success", false);
            result.put("error", ex.getMessage());
        }
        return result;
    }

    /* ============================================================ */

    private String buildApi(String gitlabUrl) {
        return trimEnd(gitlabUrl) + "/api/v4";
    }

    private HttpEntity<String> entity(String token) {
        return new HttpEntity<>(tokenHeader(token));
    }

    private HttpHeaders tokenHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null && !token.isEmpty()) {
            headers.set("PRIVATE-TOKEN", token);
        }
        return headers;
    }

    private String encode(String segment) {
        return java.net.URLEncoder.encode(segment, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String repoToPath(String repoUrl) {
        // http://host/group/project.git → group/project
        String s = repoUrl;
        int schemeEnd = s.indexOf("://");
        if (schemeEnd > 0) {
            s = s.substring(schemeEnd + 3);
        }
        int slash = s.indexOf('/');
        if (slash > 0) {
            s = s.substring(slash + 1);
        }
        if (s.endsWith(".git")) {
            s = s.substring(0, s.length() - 4);
        }
        return s;
    }

    private String trimEnd(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
