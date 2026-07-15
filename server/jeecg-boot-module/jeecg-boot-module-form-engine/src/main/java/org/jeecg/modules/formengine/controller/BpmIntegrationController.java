package org.jeecg.modules.formengine.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】BPM系统集成接口层（对接蓝凌BPM平台）---
/**
 * BPM系统集成接口层
 * 对接业务系统server与BPM平台，遵循docs/流程配置.txt规范
 * 7个对接接口：
 * 1. 获取业务表单字段接口
 * 2. 业务系统角色配置接口
 * 3. 流程事件调用业务监听
 * 4. 获取业务表单字段值接口
 * 5. 流程模板保存/更新/删除事件接口
 * 6. 流程事件的业务监听接口
 * 7. 获取业务表单模板接口
 */
@Tag(name = "BPM系统集成接口层")
@RestController
@RequestMapping("/formengine/bpm")
@Slf4j
public class BpmIntegrationController {

    /**
     * 1. 获取业务表单字段接口
     * 获取异构系统表单模板及字段定义，提供给BPM平台绑定表单或选择业务字段时配置使用
     */
    @Operation(summary = "BPM-获取业务表单字段")
    @GetMapping(value = "/formFields")
    public Result<List<Map<String, Object>>> getBusinessFormFields(@RequestParam(name = "processId") String processId,
                                                                    @RequestParam(name = "templateId", required = false) String templateId) {
        log.info("[BPM集成] 获取业务表单字段: processId={}, templateId={}", processId, templateId);
        // TODO: 实际应调用 fe_process_schema_field 表查询流程级Schema字段
        List<Map<String, Object>> fields = new ArrayList<>();
        Map<String, Object> demoField = new LinkedHashMap<>();
        demoField.put("fieldKey", "applyReason");
        demoField.put("fieldLabel", "申请原因");
        demoField.put("fieldType", "string");
        demoField.put("required", true);
        fields.add(demoField);
        return Result.OK(fields);
    }

    /**
     * 2. 业务系统角色配置接口
     * 获取业务角色，用于流程节点处理人配置
     */
    @Operation(summary = "BPM-获取业务角色")
    @GetMapping(value = "/roles")
    public Result<List<Map<String, Object>>> getBusinessRoles() {
        log.info("[BPM集成] 获取业务角色列表");
        List<Map<String, Object>> roles = new ArrayList<>();
        String[][] roleData = {
                {"applicant", "申请人", "#1890ff"},
                {"manager", "经理", "#52c41a"},
                {"finance", "财务", "#fa8c16"},
                {"admin", "管理员", "#722ed1"},
        };
        for (String[] r : roleData) {
            Map<String, Object> role = new LinkedHashMap<>();
            role.put("roleCode", r[0]);
            role.put("roleName", r[1]);
            role.put("color", r[2]);
            roles.add(role);
        }
        return Result.OK(roles);
    }

    /**
     * 3. 流程事件调用业务监听
     * 在流程运行过程中，调用配置的业务监听器，远程调用业务系统的业务服务
     * @param body { processId, eventType, nodeId, formData }
     */
    @Operation(summary = "BPM-流程事件调用业务监听")
    @PostMapping(value = "/event/listener")
    public Result<Map<String, Object>> invokeEventListener(@RequestBody Map<String, Object> body) {
        String processId = (String) body.get("processId");
        String eventType = (String) body.get("eventType");
        String nodeId = (String) body.get("nodeId");
        log.info("[BPM集成] 流程事件监听: processId={}, eventType={}, nodeId={}", processId, eventType, nodeId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("eventType", eventType);
        result.put("nodeId", nodeId);
        result.put("timestamp", new Date());
        result.put("message", "业务监听器已触发（模拟）");
        return Result.OK(result);
    }

    /**
     * 4. 获取业务表单字段值接口
     * 在流程运行过程中，获取业务系统表单的字段值
     */
    @Operation(summary = "BPM-获取业务表单字段值")
    @GetMapping(value = "/formValues")
    public Result<Map<String, Object>> getBusinessFormValues(@RequestParam(name = "processId") String processId,
                                                              @RequestParam(name = "nodeId", required = false) String nodeId,
                                                              @RequestParam(name = "role", required = false) String role) {
        log.info("[BPM集成] 获取业务表单字段值: processId={}, nodeId={}, role={}", processId, nodeId, role);
        Map<String, Object> formValues = new LinkedHashMap<>();
        formValues.put("applyReason", "测试申请原因");
        formValues.put("amount", 10000);
        formValues.put("applyDate", "2026-07-15");
        formValues.put("_nodeId", nodeId);
        formValues.put("_role", role);
        return Result.OK(formValues);
    }

    /**
     * 5. 流程模板保存、更新或删除事件接口
     * @param body { action: "save"|"update"|"delete", processId, processDef }
     */
    @Operation(summary = "BPM-流程模板事件")
    @PostMapping(value = "/template/event")
    public Result<Map<String, Object>> templateEvent(@RequestBody Map<String, Object> body) {
        String action = (String) body.get("action");
        String processId = (String) body.get("processId");
        log.info("[BPM集成] 流程模板事件: action={}, processId={}", action, processId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("action", action);
        result.put("processId", processId);
        result.put("timestamp", new Date());
        result.put("message", "流程模板" + action + "事件已同步到BPM平台（模拟）");
        return Result.OK(result);
    }

    /**
     * 6. 流程事件的业务监听接口
     * 流程运行过程中触发多个流程事件，可能触发流程变更
     * 19个回调通过doMethodProcess执行
     * @param body { processId, callbackKey, params }
     */
    @Operation(summary = "BPM-流程回调执行(doMethodProcess)")
    @PostMapping(value = "/callback")
    public Result<Map<String, Object>> executeCallback(@RequestBody Map<String, Object> body) {
        String processId = (String) body.get("processId");
        String callbackKey = (String) body.get("callbackKey");
        log.info("[BPM集成] 流程回调执行: processId={}, callbackKey={}", processId, callbackKey);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("callbackKey", callbackKey);
        result.put("processId", processId);
        result.put("timestamp", new Date());
        // 模拟回调处理结果
        result.put("data", new LinkedHashMap<>());
        result.put("message", "回调" + callbackKey + "已执行（模拟）");
        return Result.OK(result);
    }

    /**
     * 7. 获取业务表单模板接口
     * 获取业务系统的表单模板，提供给对接模块绑定表单或选择业务字段时配置使用
     */
    @Operation(summary = "BPM-获取业务表单模板")
    @GetMapping(value = "/templates")
    public Result<List<Map<String, Object>>> getBusinessTemplates() {
        log.info("[BPM集成] 获取业务表单模板列表");
        List<Map<String, Object>> templates = new ArrayList<>();
        String[][] tmplData = {
                {"tmpl_001", "客车采购申请表单", "v1.0"},
                {"tmpl_002", "售后维修派单表", "v2.1"},
                {"tmpl_003", "请假申请表", "v1.2"},
                {"tmpl_004", "费用报销单", "v1.0"},
        };
        for (String[] t : tmplData) {
            Map<String, Object> tmpl = new LinkedHashMap<>();
            tmpl.put("templateId", t[0]);
            tmpl.put("templateName", t[1]);
            tmpl.put("version", t[2]);
            templates.add(tmpl);
        }
        return Result.OK(templates);
    }

    /**
     * BPM流程操作接口（5种操作类型）
     * handler_pass=审批通过, handler_refuse=驳回, drafter_abandon=废弃,
     * handler_assign=指派, handler_communicate=沟通
     * @param body { processId, operationType, nodeId, formData, comment }
     */
    @Operation(summary = "BPM-流程操作")
    @PostMapping(value = "/operate")
    public Result<Map<String, Object>> operateProcess(@RequestBody Map<String, Object> body) {
        String processId = (String) body.get("processId");
        String operationType = (String) body.get("operationType");
        String nodeId = (String) body.get("nodeId");
        log.info("[BPM集成] 流程操作: processId={}, operationType={}, nodeId={}", processId, operationType, nodeId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("operationType", operationType);
        result.put("processId", processId);
        result.put("currentNodeId", nodeId);
        result.put("timestamp", new Date());

        // 模拟操作结果
        switch (operationType) {
            case "handler_pass":
                result.put("nextNodeId", "next_node");
                result.put("message", "审批通过，流转到下一节点");
                break;
            case "handler_refuse":
                result.put("returnNodeId", "draft_node");
                result.put("message", "已驳回至起草节点");
                break;
            case "drafter_abandon":
                result.put("processStatus", "abandoned");
                result.put("message", "流程已废弃");
                break;
            case "handler_assign":
                result.put("message", "已指派给指定人员");
                break;
            case "handler_communicate":
                result.put("message", "沟通已发起");
                break;
            default:
                result.put("message", "未知操作类型");
        }
        return Result.OK(result);
    }

    /**
     * 创建BPM流程（保存并提交流程）
     * @param body { modelId, formId, formData }
     */
    @Operation(summary = "BPM-创建流程")
    @PostMapping(value = "/createProcess")
    public Result<Map<String, Object>> createProcess(@RequestBody Map<String, Object> body) {
        String modelId = (String) body.get("modelId");
        String formId = (String) body.get("formId");
        log.info("[BPM集成] 创建BPM流程: modelId={}, formId={}", modelId, formId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("processInstanceId", "pi_" + System.currentTimeMillis());
        result.put("modelId", modelId);
        result.put("formId", formId);
        result.put("currentNodeId", "start_node");
        result.put("timestamp", new Date());
        result.put("message", "BPM流程创建成功（模拟）");
        return Result.OK(result);
    }

    /**
     * 获取BPM审批数据
     */
    @Operation(summary = "BPM-获取审批数据")
    @GetMapping(value = "/approvalData")
    public Result<Map<String, Object>> getApprovalData(@RequestParam(name = "processInstanceId") String processInstanceId) {
        log.info("[BPM集成] 获取BPM审批数据: processInstanceId={}", processInstanceId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processInstanceId", processInstanceId);
        result.put("currentNodeId", "review_node_1");
        result.put("currentNodeName", "经理审批");
        result.put("processStatus", "running");
        // 模拟审批历史
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> h1 = new LinkedHashMap<>();
        h1.put("nodeId", "start_node");
        h1.put("nodeName", "申请");
        h1.put("handler", "张三");
        h1.put("operationType", "submit");
        h1.put("comment", "提交申请");
        h1.put("time", new Date());
        history.add(h1);
        result.put("history", history);
        return Result.OK(result);
    }
}
//update-end---author:formengine ---date:2026-07-15  for：【表单引擎】BPM系统集成接口层（对接蓝凌BPM平台）---
