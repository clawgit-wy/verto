package org.jeecg.modules.formengine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.formengine.entity.FeProcessBpmApi;
import org.jeecg.modules.formengine.mapper.FeProcessBpmApiMapper;
import org.jeecg.modules.formengine.service.IFeProcessBpmApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 流程BPM对接接口配置 Service实现
 * 对应 docs/流程配置.txt 中的7个BPM对接接口
 */
@Slf4j
@Service
public class FeProcessBpmApiServiceImpl
        extends ServiceImpl<FeProcessBpmApiMapper, FeProcessBpmApi>
        implements IFeProcessBpmApiService {

    /**
     * 7个BPM对接接口的定义模板（对应docs/流程配置.txt规范）
     * 每个流程创建时自动生成这7条接口记录
     */
    private static final String[][] BPM_API_TEMPLATES = {
            // apiKey, apiName, apiMethod, apiDescription
            {"formFields", "获取业务表单字段接口", "GET", "获取异构系统表单模板及字段定义，提供给BPM平台绑定表单或选择业务字段时配置使用"},
            {"roles", "业务系统角色配置接口", "GET", "获取业务角色，用于流程节点处理人配置"},
            {"eventListener", "流程事件调用业务监听", "POST", "在流程运行过程中，调用配置的业务监听器，远程调用业务系统的业务服务"},
            {"formValues", "获取业务表单字段值接口", "GET", "在流程运行过程中，获取业务系统表单的字段值"},
            {"templateEvent", "流程模板保存/更新/删除事件接口", "POST", "流程模板保存、更新或删除时的事件通知接口"},
            {"callback", "流程事件的业务监听接口", "POST", "流程运行过程中触发多个流程事件，可能触发流程变更的业务监听接口"},
            {"templates", "获取业务表单模板接口", "GET", "获取业务系统的表单模板，提供给对接模块绑定表单或选择业务字段时配置使用"},
    };

    @Override
    public List<FeProcessBpmApi> listByProcessId(String processId) {
        return this.list(new QueryWrapper<FeProcessBpmApi>()
                .eq("process_id", processId)
                .orderByAsc("sort_no"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateBpmApisForProcess(String processId, String processCode) {
        // 先检查是否已生成过
        long existCount = this.count(new QueryWrapper<FeProcessBpmApi>().eq("process_id", processId));
        if (existCount > 0) {
            log.info("[BPM接口] 流程 {} 已存在 {} 条接口记录，跳过自动生成", processId, existCount);
            return;
        }
        // 生成7条接口记录
        Date now = new Date();
        List<FeProcessBpmApi> apiList = new ArrayList<>();
        String baseUrl = "/formengine/bpm/" + (processCode != null ? processCode.toLowerCase() : processId);
        for (int i = 0; i < BPM_API_TEMPLATES.length; i++) {
            String[] tmpl = BPM_API_TEMPLATES[i];
            FeProcessBpmApi api = new FeProcessBpmApi();
            api.setProcessId(processId);
            api.setApiKey(tmpl[0]);
            api.setApiName(tmpl[1]);
            api.setApiMethod(tmpl[2]);
            api.setApiDescription(tmpl[3]);
            // 默认URL基于流程编码生成
            api.setApiUrl(baseUrl + "/" + tmpl[0]);
            api.setSyncStatus("unsynced");
            api.setSortNo(i + 1);
            api.setCreateTime(now);
            api.setUpdateTime(now);
            apiList.add(api);
        }
        this.saveBatch(apiList);
        log.info("[BPM接口] 流程 {} 自动生成 {} 条BPM对接接口记录", processId, apiList.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApiConfig(String id, String apiUrl, String syncStatus, String syncResult) {
        FeProcessBpmApi api = this.getById(id);
        if (api == null) {
            return;
        }
        if (apiUrl != null) {
            api.setApiUrl(apiUrl);
        }
        if (syncStatus != null) {
            api.setSyncStatus(syncStatus);
        }
        if (syncResult != null) {
            api.setSyncResult(syncResult);
        }
        if ("synced".equals(syncStatus)) {
            api.setSyncTime(new Date());
        }
        api.setUpdateTime(new Date());
        this.updateById(api);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String syncToBpmPlatform(String processId) {
        List<FeProcessBpmApi> apiList = this.listByProcessId(processId);
        if (apiList.isEmpty()) {
            return "流程尚未生成BPM接口记录，请先创建流程";
        }
        int successCount = 0;
        int failCount = 0;
        StringBuilder resultMsg = new StringBuilder();
        for (FeProcessBpmApi api : apiList) {
            try {
                // TODO: 实际应调用BPM平台的配置接口，将api配置同步过去
                // 这里模拟同步成功
                api.setSyncStatus("synced");
                api.setSyncTime(new Date());
                api.setSyncResult("同步成功");
                api.setUpdateTime(new Date());
                this.updateById(api);
                successCount++;
                resultMsg.append(api.getApiName()).append(": 同步成功; ");
            } catch (Exception e) {
                api.setSyncStatus("failed");
                api.setSyncResult("同步失败: " + e.getMessage());
                api.setUpdateTime(new Date());
                this.updateById(api);
                failCount++;
                resultMsg.append(api.getApiName()).append(": 同步失败; ");
                log.error("[BPM接口] 同步失败: {}", api.getApiName(), e);
            }
        }
        log.info("[BPM接口] 流程 {} 同步完成: 成功{}条, 失败{}条", processId, successCount, failCount);
        return String.format("同步完成: 成功%d条, 失败%d条。详情: %s", successCount, failCount, resultMsg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProcessId(String processId) {
        this.remove(new QueryWrapper<FeProcessBpmApi>().eq("process_id", processId));
    }
}
