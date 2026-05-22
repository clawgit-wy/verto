package org.jeecg.modules.feplatform.webhook.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GitLab Webhook 接收端点
 * <p>
 * 注意: 路径 <code>/feplatform/webhook/**</code> 需要追加到
 * <code>shiro.excludeUrls</code> 白名单中（application-*.yml）。
 * </p>
 * <p>
 * GitLab 端 Webhook URL 示例: <br>
 * <code>http://your-host:8880/jeecg-boot/feplatform/webhook/gitlab?appId=xxx&env=test</code>
 * <br>可选请求头: <code>X-Gitlab-Token</code> 用于做简易校验（与平台保存的 token 比对，
 * 当前实现读取请求参数 token 进行比对）。
 * </p>
 *
 * @author feplatform
 */
@Slf4j
@Tag(name = "GitLab Webhook 接收")
@RestController
@RequestMapping("/feplatform/webhook")
public class FeWebhookController {

    @Autowired
    private IFePipelineService pipelineService;

    /**
     * GitLab 推送/合并事件入口：根据 appId + env 找流水线 → 触发 Jenkins 构建。
     */
    @Operation(summary = "接收GitLab Webhook")
    @PostMapping("/gitlab")
    public Result<Map<String, Object>> gitlab(
            @RequestParam(value = "appId", required = false) String appId,
            @RequestParam(value = "env", defaultValue = "test") String env,
            @RequestParam(value = "pipelineId", required = false) String pipelineId,
            @RequestHeader(value = "X-Gitlab-Event", required = false) String event,
            @RequestHeader(value = "X-Gitlab-Token", required = false) String token,
            @RequestBody(required = false) JSONObject payload,
            HttpServletRequest request) {

        log.info("[Webhook] event={} appId={} env={} pipelineId={}", event, appId, env, pipelineId);

        Map<String, Object> result = new HashMap<>();
        result.put("event", event);
        result.put("triggered", new JSONArray());

        // 1. 事件过滤：仅 Push/MergeRequest/TagPush 触发
        if (event != null && !shouldTrigger(event, payload)) {
            result.put("skipped", "event ignored: " + event);
            return Result.OK(result);
        }

        // 2. 找候选流水线
        List<FePipeline> candidates;
        if (pipelineId != null && !pipelineId.isEmpty()) {
            FePipeline one = pipelineService.getById(pipelineId);
            candidates = one == null ? List.of() : List.of(one);
        } else {
            QueryWrapper<FePipeline> qw = new QueryWrapper<>();
            qw.eq("del_flag", "0").eq("env", env);
            if (appId != null && !appId.isEmpty()) {
                qw.eq("app_id", appId);
            }
            candidates = pipelineService.list(qw);
        }

        if (candidates.isEmpty()) {
            result.put("skipped", "no pipeline matched");
            return Result.OK(result);
        }

        // 3. 逐条触发
        JSONArray triggered = new JSONArray();
        for (FePipeline p : candidates) {
            try {
                Map<String, String> params = new HashMap<>();
                if (payload != null) {
                    String sha = extractCommitSha(payload);
                    if (sha != null) {
                        params.put("COMMIT_SHA", sha);
                    }
                    String ref = payload.getString("ref");
                    if (ref != null) {
                        params.put("REF", ref);
                    }
                }
                String buildId = pipelineService.triggerBuild(p.getId(), params);
                JSONObject t = new JSONObject();
                t.put("pipelineId", p.getId());
                t.put("buildId", buildId);
                triggered.add(t);
            } catch (Exception ex) {
                log.error("[Webhook] trigger pipeline {} failed", p.getId(), ex);
            }
        }
        result.put("triggered", triggered);
        return Result.OK(result);
    }

    /**
     * Jenkins 通知回调（可选）：Jenkins Notification Plugin 可调用此地址。
     */
    @Operation(summary = "接收Jenkins构建通知")
    @PostMapping("/jenkins")
    public Result<String> jenkins(@RequestBody(required = false) JSONObject payload) {
        log.info("[Webhook] jenkins notification: {}", payload);
        return Result.OK("received");
    }

    private boolean shouldTrigger(String event, JSONObject payload) {
        if (event == null) return true;
        String e = event.toLowerCase();
        if (e.contains("push") || e.contains("tag")) {
            return true;
        }
        if (e.contains("merge request")) {
            if (payload == null) return true;
            // 仅在 MR 合并/打开时触发
            JSONObject attrs = payload.getJSONObject("object_attributes");
            if (attrs == null) return true;
            String action = attrs.getString("action");
            return "merge".equalsIgnoreCase(action) || "open".equalsIgnoreCase(action)
                    || "reopen".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action);
        }
        return false;
    }

    private String extractCommitSha(JSONObject payload) {
        // Push event
        String sha = payload.getString("after");
        if (sha != null && !sha.isEmpty()) return sha;
        // MR event
        JSONObject attrs = payload.getJSONObject("object_attributes");
        if (attrs != null) {
            return attrs.getString("last_commit") != null
                    ? attrs.getJSONObject("last_commit").getString("id")
                    : null;
        }
        return null;
    }
}
