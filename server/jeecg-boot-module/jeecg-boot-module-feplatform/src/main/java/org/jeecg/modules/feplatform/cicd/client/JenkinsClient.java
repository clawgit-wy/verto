package org.jeecg.modules.feplatform.cicd.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Jenkins REST API 客户端
 * <p>
 * 使用 Basic Auth(用户名:APIToken) 调用 Jenkins HTTP API。
 * 仅依赖项目已有的 RestTemplate，无需引入第三方 SDK。
 * </p>
 *
 * @author feplatform
 */
@Slf4j
@Component
public class JenkinsClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 触发构建（无参数 Job）。
     *
     * @return Jenkins 队列项位置（Location Header）
     */
    public String triggerBuild(FeJenkinsInstance instance, String jobName) {
        String url = buildJobUrl(instance, jobName) + "/build";
        HttpHeaders headers = authHeader(instance);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST,
                new HttpEntity<>(headers), String.class);
        return resp.getHeaders().getFirst("Location");
    }

    /**
     * 带参数触发构建。
     */
    public String triggerBuildWithParameters(FeJenkinsInstance instance, String jobName,
                                              Map<String, String> parameters) {
        String url = buildJobUrl(instance, jobName) + "/buildWithParameters";
        HttpHeaders headers = authHeader(instance);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        if (parameters != null) {
            parameters.forEach(form::add);
        }
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return resp.getHeaders().getFirst("Location");
    }

    /**
     * 中止构建。
     */
    public void stopBuild(FeJenkinsInstance instance, String jobName, int buildNo) {
        String url = buildJobUrl(instance, jobName) + "/" + buildNo + "/stop";
        try {
            restTemplate.exchange(url, HttpMethod.POST,
                    new HttpEntity<>(authHeader(instance)), String.class);
        } catch (HttpClientErrorException ex) {
            // Jenkins 中止时可能返回 302 重定向，被 RestTemplate 视为异常，可忽略
            if (ex.getStatusCode().is3xxRedirection() || ex.getStatusCode().is2xxSuccessful()) {
                return;
            }
            throw ex;
        }
    }

    /**
     * 查询单次构建详情。
     */
    public JSONObject getBuild(FeJenkinsInstance instance, String jobName, int buildNo) {
        String url = buildJobUrl(instance, jobName) + "/" + buildNo + "/api/json";
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader(instance)), String.class);
        return JSONObject.parseObject(resp.getBody());
    }

    /**
     * 查询构建控制台日志（progressiveText）。
     */
    public String getConsoleLog(FeJenkinsInstance instance, String jobName, int buildNo, long start) {
        String url = buildJobUrl(instance, jobName) + "/" + buildNo + "/logText/progressiveText?start=" + start;
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader(instance)), String.class);
        return resp.getBody();
    }

    /**
     * 拉取 Job 最近 N 次构建列表。
     */
    public JSONArray listBuilds(FeJenkinsInstance instance, String jobName, int limit) {
        String tree = String.format("builds[number,result,duration,timestamp,building,url]{0,%d}", limit);
        String url = UriComponentsBuilder.fromUriString(buildJobUrl(instance, jobName) + "/api/json")
                .queryParam("tree", tree).toUriString();
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<>(authHeader(instance)), String.class);
        JSONObject obj = JSONObject.parseObject(resp.getBody());
        return obj == null ? new JSONArray() : obj.getJSONArray("builds");
    }

    /**
     * 创建/更新 Job：使用传入的 config.xml（Jenkins 原生配置 XML）。
     */
    public void createOrUpdateJob(FeJenkinsInstance instance, String jobName, String configXml) {
        String base = trimEnd(instance.getUrl());
        // 先尝试更新，若 404 再创建
        String updateUrl = base + "/job/" + jobName + "/config.xml";
        HttpHeaders headers = authHeader(instance);
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> entity = new HttpEntity<>(configXml, headers);
        try {
            restTemplate.exchange(updateUrl, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException.NotFound e) {
            String createUrl = UriComponentsBuilder.fromUriString(base + "/createItem")
                    .queryParam("name", jobName).toUriString();
            restTemplate.exchange(createUrl, HttpMethod.POST, entity, String.class);
        }
    }

    /**
     * 连通性测试（GET /api/json）。
     */
    public Map<String, Object> ping(FeJenkinsInstance instance) {
        Map<String, Object> result = new HashMap<>();
        try {
            String url = trimEnd(instance.getUrl()) + "/api/json";
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(authHeader(instance)), String.class);
            result.put("success", resp.getStatusCode().is2xxSuccessful());
            result.put("status", resp.getStatusCode().value());
            result.put("body", resp.getBody());
        } catch (Exception ex) {
            log.warn("Jenkins ping failed: {}", ex.getMessage());
            result.put("success", false);
            result.put("error", ex.getMessage());
        }
        return result;
    }

    /* ============================================================ */

    private String buildJobUrl(FeJenkinsInstance instance, String jobName) {
        return trimEnd(instance.getUrl()) + "/job/" + jobName;
    }

    private HttpHeaders authHeader(FeJenkinsInstance instance) {
        HttpHeaders headers = new HttpHeaders();
        String token = instance.getToken();
        if (token != null && !token.isEmpty()) {
            // 约定：token 以 "user:token" 形式存储，便于使用用户名 + API Token Basic Auth
            String basic = token.contains(":") ? token : ("admin:" + token);
            String encoded = Base64.getEncoder()
                    .encodeToString(basic.getBytes(StandardCharsets.UTF_8));
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        }
        return headers;
    }

    private String trimEnd(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
