package org.jeecg.modules.formengine.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.formengine.entity.FeFormField;
import org.jeecg.modules.formengine.entity.FeFormMethod;
import org.jeecg.modules.formengine.entity.FeFormTemplate;
import org.jeecg.modules.formengine.entity.FeProcess;
import org.jeecg.modules.formengine.entity.FeProcessSchemaField;
import org.jeecg.modules.formengine.entity.FeProcessBpmApi;
import org.jeecg.modules.formengine.service.IFeFormFieldService;
import org.jeecg.modules.formengine.service.IFeFormMethodService;
import org.jeecg.modules.formengine.service.IFeFormTemplateService;
import org.jeecg.modules.formengine.service.IFeProcessService;
import org.jeecg.modules.formengine.service.IFeProcessSchemaFieldService;
import org.jeecg.modules.formengine.service.IFeProcessBpmApiService;
import org.jeecg.modules.formengine.vo.DoMethodVO;
import org.jeecg.modules.formengine.vo.ImportProcessVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】表单引擎工作台Controller---
@Tag(name = "表单引擎工作台")
@RestController
@RequestMapping("/formengine")
@Slf4j
public class FormEngineController extends JeecgController<FeProcess, IFeProcessService> {

    @Autowired
    private IFeProcessService feProcessService;

    @Autowired
    private IFeFormTemplateService feFormTemplateService;

    @Autowired
    private IFeFormFieldService feFormFieldService;

    @Autowired
    private IFeFormMethodService feFormMethodService;

    // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】注入流程级Schema字段Service---
    @Autowired
    private IFeProcessSchemaFieldService feProcessSchemaFieldService;
    // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】注入流程级Schema字段Service---

    // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】注入BPM对接接口Service---
    @Autowired
    private IFeProcessBpmApiService feProcessBpmApiService;
    // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】注入BPM对接接口Service---

    @Operation(summary = "流程-分页列表查询")
    @GetMapping(value = "/process/list")
    public Result<IPage<FeProcess>> queryPageList(FeProcess feProcess,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  HttpServletRequest req) {
        QueryWrapper<FeProcess> queryWrapper = QueryGenerator.initQueryWrapper(feProcess, req.getParameterMap());
        queryWrapper.eq("del_flag", "0");
        queryWrapper.orderByDesc("update_time");
        Page<FeProcess> page = new Page<>(pageNo, pageSize);
        IPage<FeProcess> pageList = feProcessService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @AutoLog(value = "流程-添加")
    @Operation(summary = "流程-添加")
    @PostMapping(value = "/process/add")
    public Result<String> add(@RequestBody FeProcess feProcess) {
        if (feProcess.getStatus() == null) {
            feProcess.setStatus("draft");
        }
        Date now = new Date();
        feProcess.setCreateTime(now);
        feProcess.setUpdateTime(now);
        feProcessService.save(feProcess);
        // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】创建流程时自动生成7个BPM对接接口记录---
        feProcessBpmApiService.generateBpmApisForProcess(feProcess.getId(), feProcess.getProcessCode());
        // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】创建流程时自动生成7个BPM对接接口记录---
        return Result.OK("添加成功");
    }

    @AutoLog(value = "流程-编辑")
    @Operation(summary = "流程-编辑")
    @RequestMapping(value = "/process/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody FeProcess feProcess) {
        feProcess.setUpdateTime(new Date());
        feProcessService.updateById(feProcess);
        return Result.OK("编辑成功");
    }

    @AutoLog(value = "流程-通过id删除")
    @Operation(summary = "流程-通过id删除")
    @DeleteMapping(value = "/process/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        feProcessService.removeById(id);
        return Result.OK("删除成功");
    }

    @Operation(summary = "流程-通过id查询")
    @GetMapping(value = "/process/queryById")
    public Result<FeProcess> queryById(@RequestParam(name = "id", required = true) String id) {
        FeProcess feProcess = feProcessService.getById(id);
        // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】queryById时附带schemaFields列表---
        if (feProcess != null) {
            List<FeProcessSchemaField> schemaFields = feProcessSchemaFieldService.listByProcessId(id);
            feProcess.setFormSchemaFields(JSON.toJSONString(schemaFields));
        }
        // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】queryById时附带schemaFields列表---
        return Result.OK(feProcess);
    }

    @Operation(summary = "表单模版-列表查询")
    @GetMapping(value = "/template/list")
    public Result<List<FeFormTemplate>> getTemplateFormList() {
        List<FeFormTemplate> list = feFormTemplateService.list();
        return Result.OK(list);
    }

    @Operation(summary = "表单字段-列表查询")
    @GetMapping(value = "/field/list")
    public Result<List<FeFormField>> getFormFieldList(@RequestParam(name = "templateId") String templateId) {
        List<FeFormField> list = feFormFieldService.list(new QueryWrapper<FeFormField>().eq("template_id", templateId).orderByAsc("sort_no"));
        return Result.OK(list);
    }

    @Operation(summary = "表单字段值-按角色查询")
    @GetMapping(value = "/field/value/list")
    public Result<List<Map<String, Object>>> getFormFieldValueList(@RequestParam(name = "templateId") String templateId,
                                                                   @RequestParam(name = "role", required = false, defaultValue = "applicant") String role) {
        List<FeFormField> fields = feFormFieldService.list(new QueryWrapper<FeFormField>().eq("template_id", templateId).orderByAsc("sort_no"));
        List<Map<String, Object>> list = new ArrayList<>();
        if (fields != null) {
            for (FeFormField field : fields) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("fieldKey", field.getFieldKey());
                item.put("fieldLabel", field.getFieldLabel());
                item.put("fieldType", field.getFieldType());
                item.put("defaultValue", field.getDefaultValue());
                String permission = null;
                if (field.getPermissions() != null && field.getPermissions().trim().length() > 0) {
                    try {
                        JSONObject perm = JSONObject.parseObject(field.getPermissions());
                        if (perm != null) {
                            permission = perm.getString(role);
                        }
                    } catch (Exception ignored) {
                    }
                }
                item.put("permission", permission);
                list.add(item);
            }
        }
        return Result.OK(list);
    }

    @Operation(summary = "表单方法-列表查询")
    @GetMapping(value = "/method/list")
    public Result<List<FeFormMethod>> getMethodInfo() {
        List<FeFormMethod> list = feFormMethodService.list();
        return Result.OK(list);
    }

    @Operation(summary = "表单方法-执行")
    @PostMapping(value = "/method/do")
    public Result<Map<String, Object>> doMethodProcess(@RequestBody DoMethodVO vo) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("methodKey", vo.getMethodKey());
        result.put("success", true);
        String methodKey = vo.getMethodKey();
        if (methodKey != null && (methodKey.contains("calculateDiscount") || methodKey.contains("calculateTotal"))) {
            int orderCount = 0;
            if (vo.getParams() != null) {
                Object oc = vo.getParams().get("order_count");
                if (oc != null) {
                    if (oc instanceof Number) {
                        orderCount = ((Number) oc).intValue();
                    } else {
                        orderCount = Integer.parseInt(String.valueOf(oc));
                    }
                }
            }
            int unitPrice = 120000;
            result.put("unit_price", unitPrice);
            result.put("total_price", orderCount * unitPrice);
        } else if (methodKey != null && methodKey.contains("checkInventory")) {
            result.put("in_stock", true);
            result.put("message", "库存充足");
        }
        return Result.OK(result);
    }

    @Operation(summary = "流程定义-导入")
    @PostMapping(value = "/process/import")
    public Result<?> importProcessDefinition(@RequestBody ImportProcessVO vo) {
        try {
            JSONObject json = JSON.parseObject(vo.getProcessDefJson());
            JSONArray nodes = json != null ? json.getJSONArray("nodes") : null;
            List<Object> nodeList = nodes != null ? nodes : new ArrayList<>();
            FeProcess feProcess = new FeProcess();
            feProcess.setId(vo.getProcessId());
            feProcess.setProcessDef(vo.getProcessDefJson());
            feProcess.setStatus("imported");
            feProcess.setUpdateTime(new Date());
            feProcessService.updateById(feProcess);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("processId", vo.getProcessId());
            map.put("nodeCount", nodeList.size());
            map.put("nodes", nodeList);
            map.put("status", "imported");
            return Result.OK(map);
        } catch (Exception e) {
            return Result.error("流程定义JSON解析失败: " + e.getMessage());
        }
    }

    // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、前置检查、初始化渲染接口---

    /**
     * 流程级Schema字段-列表查询
     * 根据流程ID查询该流程的所有表单字段定义
     */
    @Operation(summary = "流程级Schema字段-列表查询")
    @GetMapping(value = "/schema/list")
    public Result<List<FeProcessSchemaField>> listSchemaFields(@RequestParam(name = "processId") String processId) {
        List<FeProcessSchemaField> list = feProcessSchemaFieldService.listByProcessId(processId);
        return Result.OK(list);
    }

    /**
     * 流程级Schema字段-批量保存
     * 前端传入完整的字段列表，后端先删除旧的再批量插入
     */
    @AutoLog(value = "流程级Schema字段-批量保存")
    @Operation(summary = "流程级Schema字段-批量保存")
    @PostMapping(value = "/schema/saveBatch")
    public Result<?> saveSchemaFields(@RequestBody Map<String, Object> body) {
        String processId = (String) body.get("processId");
        if (processId == null || processId.trim().isEmpty()) {
            return Result.error("缺少流程ID");
        }
        Object fieldsObj = body.get("fields");
        if (!(fieldsObj instanceof List)) {
            return Result.error("fields字段必须为数组");
        }
        JSONArray fieldsArray = JSON.parseArray(JSON.toJSONString(fieldsObj));
        List<FeProcessSchemaField> fields = new ArrayList<>();
        for (int i = 0; i < fieldsArray.size(); i++) {
            JSONObject obj = fieldsArray.getJSONObject(i);
            FeProcessSchemaField field = new FeProcessSchemaField();
            field.setFieldKey(obj.getString("fieldKey"));
            field.setFieldLabel(obj.getString("fieldLabel"));
            field.setFieldType(obj.getString("fieldType"));
            field.setDefaultValue(obj.getString("defaultValue"));
            field.setRequired(obj.getBooleanValue("required") ? 1 : 0);
            field.setPattern(obj.getString("pattern"));
            field.setMinValue(obj.getInteger("min"));
            field.setMaxValue(obj.getInteger("max"));
            field.setValidationMessage(obj.getString("validationMessage"));
            field.setDescription(obj.getString("description"));
            field.setSortNo(i + 1);
            fields.add(field);
        }
        // 校验字段标识不能重复
        long distinctCount = fields.stream().map(FeProcessSchemaField::getFieldKey).distinct().count();
        if (distinctCount != fields.size()) {
            return Result.error("字段标识存在重复，请检查");
        }
        // 校验字段标识不能为空
        for (FeProcessSchemaField f : fields) {
            if (f.getFieldKey() == null || f.getFieldKey().trim().isEmpty()) {
                return Result.error("存在字段标识为空的配置，请填写");
            }
        }
        // 批量保存
        feProcessSchemaFieldService.saveBatchByProcessId(processId, fields);
        // 同步更新 fe_process 表的 form_schema_fields 缓存字段
        FeProcess feProcess = feProcessService.getById(processId);
        if (feProcess != null) {
            feProcess.setFormSchemaFields(JSON.toJSONString(fields));
            feProcess.setUpdateTime(new Date());
            feProcessService.updateById(feProcess);
        }
        return Result.OK("保存成功（共" + fields.size() + "个字段）");
    }

    /**
     * 流程级Schema字段-删除
     */
    @AutoLog(value = "流程级Schema字段-删除")
    @Operation(summary = "流程级Schema字段-删除")
    @DeleteMapping(value = "/schema/delete")
    public Result<?> deleteSchemaFields(@RequestParam(name = "processId") String processId) {
        feProcessSchemaFieldService.deleteByProcessId(processId);
        // 清空 fe_process 表的 form_schema_fields 缓存
        FeProcess feProcess = feProcessService.getById(processId);
        if (feProcess != null) {
            feProcess.setFormSchemaFields(null);
            feProcess.setUpdateTime(new Date());
            feProcessService.updateById(feProcess);
        }
        return Result.OK("删除成功");
    }

    /**
     * 设计器前置检查
     * 检查流程是否满足进入设计器的前置条件：
     * 1. 必须已导入流程定义（processDef 非空）
     * 2. 必须已配置表单Schema字段（schemaFields 非空）
     * 返回 { canEnter: true/false, hasProcessDef: bool, hasSchemaFields: bool, message: "" }
     */
    @Operation(summary = "设计器前置检查")
    @GetMapping(value = "/designer/check")
    public Result<Map<String, Object>> checkDesignerPrecondition(@RequestParam(name = "processId") String processId) {
        Map<String, Object> result = new LinkedHashMap<>();
        FeProcess feProcess = feProcessService.getById(processId);
        boolean hasProcessDef = feProcess != null && feProcess.getProcessDef() != null
                && !feProcess.getProcessDef().trim().isEmpty();
        List<FeProcessSchemaField> schemaFields = feProcessSchemaFieldService.listByProcessId(processId);
        boolean hasSchemaFields = schemaFields != null && !schemaFields.isEmpty();
        boolean canEnter = hasProcessDef && hasSchemaFields;

        result.put("canEnter", canEnter);
        result.put("hasProcessDef", hasProcessDef);
        result.put("hasSchemaFields", hasSchemaFields);
        result.put("schemaFieldCount", schemaFields != null ? schemaFields.size() : 0);

        StringBuilder message = new StringBuilder();
        if (!hasProcessDef) {
            message.append("尚未导入流程定义XML，请先在流程列表点击「导入」按钮导入蓝凌BPM流程定义；");
        }
        if (!hasSchemaFields) {
            message.append("尚未配置表单Schema字段，请先在流程列表点击「Schema」按钮配置流程级表单字段；");
        }
        if (canEnter) {
            message.append("前置条件已满足，可以进入设计器");
        }
        result.put("message", message.toString());
        return Result.OK(result);
    }

    /**
     * 设计器初始化渲染
     * 根据已配置的表单Schema字段自动生成初始画布布局
     * 将每个Schema字段映射为对应的表单组件（基于FIELD_TYPE_TO_COMPONENT映射）
     */
    @Operation(summary = "设计器初始化渲染")
    @GetMapping(value = "/designer/init")
    public Result<Map<String, Object>> initDesignerLayout(@RequestParam(name = "processId") String processId) {
        Map<String, Object> result = new LinkedHashMap<>();
        // 1. 加载流程节点
        FeProcess feProcess = feProcessService.getById(processId);
        List<Object> processNodes = new ArrayList<>();
        if (feProcess != null && feProcess.getProcessDef() != null) {
            try {
                JSONObject processDefJson = JSON.parseObject(feProcess.getProcessDef());
                if (processDefJson != null && processDefJson.containsKey("nodes")) {
                    processNodes = processDefJson.getJSONArray("nodes");
                }
            } catch (Exception ignored) {
            }
        }
        result.put("processNodes", processNodes);

        // 2. 根据Schema字段生成初始画布布局
        List<FeProcessSchemaField> schemaFields = feProcessSchemaFieldService.listByProcessId(processId);
        JSONArray layout = new JSONArray();
        if (schemaFields != null) {
            for (FeProcessSchemaField field : schemaFields) {
                JSONObject comp = new JSONObject();
                // 字段类型 -> 组件类型映射
                String compType = mapFieldTypeToComponent(field.getFieldType());
                comp.put("type", compType);
                comp.put("field", field.getFieldKey());
                comp.put("title", field.getFieldLabel() != null ? field.getFieldLabel() : field.getFieldKey());
                // props
                JSONObject props = new JSONObject();
                props.put("placeholder", "请输入" + (field.getFieldLabel() != null ? field.getFieldLabel() : ""));
                if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
                    props.put("defaultValue", field.getDefaultValue());
                }
                comp.put("props", props);
                // validate
                JSONArray validate = new JSONArray();
                if (field.getRequired() != null && field.getRequired() == 1) {
                    JSONObject requiredRule = new JSONObject();
                    requiredRule.put("required", true);
                    requiredRule.put("message", (field.getFieldLabel() != null ? field.getFieldLabel() : field.getFieldKey()) + "为必填项");
                    requiredRule.put("trigger", "blur");
                    validate.add(requiredRule);
                }
                comp.put("validate", validate);
                // 默认权限
                JSONObject permissions = new JSONObject();
                permissions.put("applicant", "write");
                permissions.put("manager", "readonly");
                permissions.put("finance", "readonly");
                comp.put("permissions", permissions);
                comp.put("nodePermissions", new JSONObject());
                layout.add(comp);
            }
        }
        result.put("layout", layout);
        result.put("schemaFields", schemaFields);
        result.put("message", layout.isEmpty() ? "暂无Schema字段，无法生成初始布局" : "已根据" + layout.size() + "个Schema字段生成初始布局");
        return Result.OK(result);
    }

    /**
     * 字段类型 -> 组件类型映射
     * string -> input, number -> number, boolean -> switch, date -> date,
     * stringArray -> select, numberArray -> select
     */
    private String mapFieldTypeToComponent(String fieldType) {
        if (fieldType == null) return "input";
        switch (fieldType) {
            case "string": return "input";
            case "number": return "number";
            case "boolean": return "switch";
            case "date": return "date";
            case "stringArray": return "select";
            case "numberArray": return "select";
            default: return "input";
        }
    }
    // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、前置检查、初始化渲染接口---

    // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口管理（查询/更新/同步）---

    /**
     * BPM对接接口-列表查询
     * 查询指定流程的7个BPM对接接口配置
     */
    @Operation(summary = "BPM对接接口-列表查询")
    @GetMapping(value = "/bpmApi/list")
    public Result<List<FeProcessBpmApi>> listBpmApis(@RequestParam(name = "processId") String processId) {
        List<FeProcessBpmApi> list = feProcessBpmApiService.listByProcessId(processId);
        return Result.OK(list);
    }

    /**
     * BPM对接接口-更新URL配置
     * 用户可修改接口的访问URL
     */
    @AutoLog(value = "BPM对接接口-更新配置")
    @Operation(summary = "BPM对接接口-更新URL配置")
    @PostMapping(value = "/bpmApi/update")
    public Result<?> updateBpmApi(@RequestBody FeProcessBpmApi bpmApi) {
        if (bpmApi.getId() == null) {
            return Result.error("缺少接口ID");
        }
        feProcessBpmApiService.updateApiConfig(bpmApi.getId(), bpmApi.getApiUrl(), null, null);
        return Result.OK("更新成功");
    }

    /**
     * BPM对接接口-批量同步到BPM平台
     * 将流程的7个接口配置同步到BPM平台
     */
    @AutoLog(value = "BPM对接接口-同步到BPM平台")
    @Operation(summary = "BPM对接接口-批量同步到BPM平台")
    @PostMapping(value = "/bpmApi/sync")
    public Result<Map<String, Object>> syncBpmApis(@RequestParam(name = "processId") String processId) {
        String resultMsg = feProcessBpmApiService.syncToBpmPlatform(processId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("processId", processId);
        result.put("syncResult", resultMsg);
        return Result.OK(result);
    }

    /**
     * BPM对接接口-为已存在的流程补生成接口记录
     * 针对之前创建的、未自动生成接口的流程
     */
    @AutoLog(value = "BPM对接接口-补生成")
    @Operation(summary = "BPM对接接口-为流程补生成接口记录")
    @PostMapping(value = "/bpmApi/generate")
    public Result<?> generateBpmApis(@RequestParam(name = "processId") String processId) {
        FeProcess process = feProcessService.getById(processId);
        if (process == null) {
            return Result.error("流程不存在");
        }
        feProcessBpmApiService.generateBpmApisForProcess(processId, process.getProcessCode());
        return Result.OK("接口记录生成成功");
    }
    // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口管理（查询/更新/同步）---
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】表单引擎工作台Controller---
