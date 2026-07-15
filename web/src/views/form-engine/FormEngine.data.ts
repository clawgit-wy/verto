import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】数据定义文件，对齐JeecgBoot标准---
// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】对齐业务实际流程状态码（00废弃/10草稿/20审批中/30通过/40已发布）---
/**
 * 流程状态映射 - 对齐蓝凌BPM业务状态码
 * 00=废弃, 10=草稿/驳回, 20=审批中/待审核, 30=通过/结束, 40=已发布
 */
export const PROCESS_STATUS = {
  '00': { label: '废弃', color: 'red' },
  '10': { label: '草稿', color: 'default' },
  '20': { label: '审批中', color: 'processing' },
  '30': { label: '已通过', color: 'success' },
  '40': { label: '已发布', color: 'green' },
  // 兼容旧状态值
  draft: { label: '草稿', color: 'default' },
  imported: { label: '已导入', color: 'processing' },
  simulated: { label: '已仿真', color: 'warning' },
  exported: { label: '已导出', color: 'success' },
} as Record<string, { label: string; color: string }>;
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】对齐业务实际流程状态码（00废弃/10草稿/20审批中/30通过/40已发布）---

/** 流程列表列定义 */
export const processColumns: BasicColumn[] = [
  {
    title: '流程名称',
    dataIndex: 'processName',
    width: 180,
    align: 'left',
  },
  {
    title: '流程编码',
    dataIndex: 'processCode',
    width: 140,
  },
  {
    title: '表单模板',
    dataIndex: 'templateName',
    width: 160,
  },
  {
    title: '版本',
    dataIndex: 'version',
    width: 80,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    slots: { customRender: 'status' },
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    width: 160,
  },
];

/** 搜索表单 schema */
export const processSearchSchema: FormSchema[] = [
  {
    label: '流程名称',
    field: 'processName',
    component: 'Input',
    componentProps: { placeholder: '请输入流程名称' },
    colProps: { span: 6 },
  },
  {
    label: '流程编码',
    field: 'processCode',
    component: 'Input',
    componentProps: { placeholder: '请输入流程编码' },
    colProps: { span: 6 },
  },
  {
    label: '状态',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: [
        { label: '草稿', value: 'draft' },
        { label: '已导入', value: 'imported' },
        { label: '已仿真', value: 'simulated' },
        { label: '已导出', value: 'exported' },
      ],
      placeholder: '请选择状态',
    },
    colProps: { span: 6 },
  },
];

/** 新建/编辑流程表单 schema */
export const processFormSchema: FormSchema[] = [
  { field: 'id', label: '', component: 'Input', show: false },
  {
    field: 'processName',
    label: '流程名称',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入流程名称' },
    colProps: { span: 24 },
  },
  {
    field: 'processCode',
    label: '流程编码',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '如 PROC_BUS_001' },
    colProps: { span: 24 },
  },
  {
    field: 'templateId',
    label: '表单模板',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      api: () => import('/@/api/form-engine').then((m) => m.getTemplateFormList()),
      labelField: 'templateName',
      valueField: 'id',
      placeholder: '请选择表单模板',
    },
    colProps: { span: 24 },
  },
  {
    field: 'version',
    label: '版本',
    component: 'Input',
    defaultValue: 'v1.0',
    componentProps: { placeholder: '如 v1.0' },
    colProps: { span: 12 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    defaultValue: 'draft',
    componentProps: {
      options: [
        { label: '草稿', value: 'draft' },
        { label: '已导入', value: 'imported' },
        { label: '已仿真', value: 'simulated' },
        { label: '已导出', value: 'exported' },
      ],
    },
    colProps: { span: 12 },
  },
];

/** 组件类型列表（设计器用） */
export const COMPONENT_TYPES = [
  { type: 'input', label: '单行文本', icon: 'ant-design:edit-outlined', group: '基础字段' },
  { type: 'textarea', label: '多行文本', icon: 'ant-design:align-left-outlined', group: '基础字段' },
  { type: 'number', label: '数字', icon: 'ant-design:field-number-outlined', group: '基础字段' },
  { type: 'select', label: '下拉选择', icon: 'ant-design:down-circle-outlined', group: '基础字段' },
  { type: 'radio', label: '单选框', icon: 'ant-design:check-circle-outlined', group: '基础字段' },
  { type: 'checkbox', label: '多选框', icon: 'ant-design:check-square-outlined', group: '基础字段' },
  { type: 'date', label: '日期', icon: 'ant-design:calendar-outlined', group: '基础字段' },
  { type: 'switch', label: '开关', icon: 'ant-design:swap-outlined', group: '基础字段' },
  { type: 'button', label: '按钮', icon: 'ant-design:thunderbolt-outlined', group: '操作组件' },
  { type: 'title', label: '标题', icon: 'ant-design:font-size-outlined', group: '布局组件' },
  { type: 'divider', label: '分割线', icon: 'ant-design:minus-outlined', group: '布局组件' },
  { type: 'alert', label: '提示', icon: 'ant-design:info-circle-outlined', group: '布局组件' },
];

/** 角色列表（仿真用） */
export const SIMULATION_ROLES = [
  { value: 'applicant', label: '申请人', color: 'blue' },
  { value: 'manager', label: '部门经理', color: 'orange' },
  { value: 'finance', label: '财务总监', color: 'green' },
];

/** 权限类型（角色权限，兼容旧版） */
export const PERMISSION_TYPES = [
  { value: 'write', label: '可写', color: 'green' },
  { value: 'readonly', label: '只读', color: 'blue' },
  { value: 'hidden', label: '隐藏', color: 'red' },
];

// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】流程节点字段权限控制 auth=0可编辑/1不可见/2只读---
/** 节点字段权限类型（auth 值） */
export const AUTH_TYPES = [
  { value: 0, label: '可编辑', color: 'green' },
  { value: 1, label: '不可见', color: 'red' },
  { value: 2, label: '只读', color: 'blue' },
];

/** auth 值 -> 中文标签 */
export function getAuthLabel(auth: number): string {
  const found = AUTH_TYPES.find((a) => a.value === auth);
  return found ? found.label : '未知';
}

/** auth 值 -> 颜色 */
export function getAuthColor(auth: number): string {
  const found = AUTH_TYPES.find((a) => a.value === auth);
  return found ? found.color : 'default';
}
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】流程节点字段权限控制 auth=0可编辑/1不可见/2只读---

// update-begin---author:formengine ---date:2026-07-13  for：【表单引擎】节点级表单Schema字段配置数据定义---
/**
 * 表单字段类型 - 对齐业务系统六种字段类型
 * 字符/数字/布尔/日期/字符数组/数字数组
 */
export const FIELD_TYPES = [
  { value: 'string', label: '字符', desc: '单行/多行文本' },
  { value: 'number', label: '数字', desc: '整数或小数' },
  { value: 'boolean', label: '布尔', desc: '是/否' },
  { value: 'date', label: '日期', desc: '日期或日期时间' },
  { value: 'stringArray', label: '字符数组', desc: '字符串列表' },
  { value: 'numberArray', label: '数字数组', desc: '数字列表' },
] as const;

/** 字段类型 -> 对应的设计器组件类型映射 */
export const FIELD_TYPE_TO_COMPONENT: Record<string, string[]> = {
  string: ['input', 'textarea'],
  number: ['number'],
  boolean: ['switch'],
  date: ['date'],
  stringArray: ['select', 'checkbox'],
  numberArray: ['select', 'checkbox'],
};

/** 验证规则类型 */
export const VALIDATION_TYPES = [
  { value: 'required', label: '必填', desc: '字段不能为空' },
  { value: 'pattern', label: '正则校验', desc: '自定义正则表达式' },
  { value: 'min', label: '最小值/最小长度', desc: '数字最小值或字符最小长度' },
  { value: 'max', label: '最大值/最大长度', desc: '数字最大值或字符最大长度' },
  { value: 'email', label: '邮箱格式', desc: '校验邮箱格式' },
  { value: 'phone', label: '手机号', desc: '校验手机号格式' },
];

/**
 * 节点表单Schema字段定义
 * 每个流程节点可配置一组表单字段，用于在设计器中与画布组件关联
 */
export interface NodeFormField {
  /** 字段标识（唯一key） */
  fieldKey: string;
  /** 显示名称 */
  fieldLabel: string;
  /** 字段类型: string/number/boolean/date/stringArray/numberArray */
  fieldType: string;
  /** 默认值 */
  defaultValue?: string;
  /** 是否必填 */
  required: boolean;
  /** 正则校验表达式 */
  pattern?: string;
  /** 最小值（数字）或最小长度（字符） */
  min?: number;
  /** 最大值（数字）或最大长度（字符） */
  max?: number;
  /** 验证失败提示信息 */
  validationMessage?: string;
  /** 字段说明 */
  description?: string;
}

/** 节点表单Schema映射: { [nodeId]: NodeFormField[] } */
export type NodeFormSchemas = Record<string, NodeFormField[]>;

/** 获取字段类型的中文标签 */
export function getFieldTypeLabel(type: string): string {
  const found = FIELD_TYPES.find((t) => t.value === type);
  return found ? found.label : type;
}

/** 获取字段类型的颜色标签 */
export function getFieldTypeColor(type: string): string {
  const colorMap: Record<string, string> = {
    string: 'blue',
    number: 'green',
    boolean: 'orange',
    date: 'geekblue',
    stringArray: 'purple',
    numberArray: 'magenta',
  };
  return colorMap[type] || 'default';
}
// update-end---author:formengine ---date:2026-07-13  for：【表单引擎】节点级表单Schema字段配置数据定义---

/** UI 框架选项（代码导出用） */
export const UI_FRAMEWORKS = [
  { value: 'element', label: 'Element Plus', tagPrefix: 'el-', formComponent: 'el-form', itemComponent: 'el-form-item' },
  { value: 'antd', label: 'Ant Design Vue', tagPrefix: 'a-', formComponent: 'a-form', itemComponent: 'a-form-item' },
  { value: 'naive', label: 'Naive UI', tagPrefix: 'n-', formComponent: 'n-form', itemComponent: 'n-form-item' },
];

/** 代码风格选项 */
export const CODE_STYLES = [
  { value: 'setup', label: 'script setup（推荐）' },
  { value: 'options', label: 'Options API' },
];
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】数据定义文件，对齐JeecgBoot标准---

// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】对齐业务实际流程引擎配置（蓝凌LBPM 12个业务流程模型）---
/**
 * 业务流程模型清单 - 对齐 bpm.md 中的 12 个业务流程
 * 每个流程对应蓝凌BPM的一个 modelId + formId 组合
 */
export interface BpmProcessModel {
  modelId: string;       // BPM 模型ID
  formId: string;        // 表单ID
  processName: string;   // 流程名称
  category: string;      // 业务分类
  description: string;   // 业务场景说明
}

export const BPM_PROCESS_MODELS: BpmProcessModel[] = [
  { modelId: 'build', formId: 'safety_build_process', processName: '楼宇门禁申请流程', category: '门禁管理', description: '员工申请楼宇门禁权限（开通/关闭）' },
  { modelId: 'build_manage', formId: 'safety_build_manage_process', processName: '楼宇门禁管理流程', category: '门禁管理', description: '门禁系统管理员/保密负责人调整责任部门' },
  { modelId: 'park', formId: 'safety_park_power_process', processName: '人车权限申请流程', category: '门禁管理', description: '员工申请厂区大门权限（人脸/卡/车）' },
  { modelId: 'goods_out', formId: 'safety_goods_out_process', processName: '物资出门证流程', category: '物资管理', description: '货物出厂审批（含废料销售、物资出门）' },
  { modelId: 'goods_out_events', formId: 'safety_goods_out_event_process', processName: '出门证异常事件流程', category: '物资管理', description: '出门证违规事件上报处理' },
  { modelId: 'spot_check', formId: 'safety_spot_check_process', processName: '设备点检发布流程', category: '巡检审批', description: '设备设施点检任务发布审批' },
  { modelId: 'monitor_post', formId: 'safety_monitor_post_process', processName: '监控岗发布流程', category: '巡检审批', description: '监控岗位任务发布审批' },
  { modelId: 'process_patrol', formId: 'safety_process_patrol', processName: '治安巡查发布流程', category: '巡检审批', description: '治安巡查任务发布审批' },
  { modelId: 'outsider_promise', formId: 'safety_outsider_promise_change', processName: '外协承诺变更流程', category: '人员管理', description: '外来人员承诺信息变更' },
  { modelId: 'enter', formId: 'safety_enter_filing', processName: '入场备案流程', category: '人员管理', description: '黑名单人员特殊入厂备案' },
  { modelId: 'entry_blank', formId: 'safety_entry_blank_process', processName: '黑名单入场申请流程', category: '人员管理', description: '黑名单人员入厂申请' },
  { modelId: 'visitor', formId: 'safety_visitor_authorization_approval', processName: '访客授权审批流程', category: '人员管理', description: '访客授权申请审批' },
];

/**
 * BPM 回调函数配置 - 对齐 bpm.md 中 doMethodProcess 的 16 个回调函数
 */
export interface BpmCallback {
  functionId: string;    // 回调函数ID
  functionName: string;  // 函数名称
  modelId: string;       // 关联流程模型ID
  description: string;   // 回调场景说明
}

export const BPM_CALLBACKS: BpmCallback[] = [
  { functionId: 'safety_spot_check_publish', functionName: '设备点检发布', modelId: 'spot_check', description: '更新状态为RELEASE，记录发布时间' },
  { functionId: 'safety_monitor_post_publish', functionName: '监控岗发布', modelId: 'monitor_post', description: '更新状态为RELEASE' },
  { functionId: 'safety_process_patrol_publish', functionName: '治安巡查发布', modelId: 'process_patrol', description: '更新节点为N3（结束）' },
  { functionId: 'safety_outsider_promise_change', functionName: '外协承诺变更', modelId: 'outsider_promise', description: '更新外协流程状态' },
  { functionId: 'safety_enter_filing_state_20', functionName: '入场备案待审', modelId: 'enter', description: '更新备案状态为20（待审核）' },
  { functionId: 'safety_enter_filing_state_30', functionName: '入场备案通过', modelId: 'enter', description: '更新备案状态为30（通过）' },
  { functionId: 'safety_enter_filing_state_40', functionName: '入场备案发布', modelId: 'enter', description: '更新备案状态为40（发布），下发权限' },
  { functionId: 'safety_enter_filing_pass', functionName: '入场备案权限下发', modelId: 'enter', description: '调用第三方接口下发权限' },
  { functionId: 'safety_goods_out_process', functionName: '物资出门证发布', modelId: 'goods_out', description: '更新出门证状态，记录发布时间' },
  { functionId: 'safety_goods_out_events_process', functionName: '出门证异常事件', modelId: 'goods_out_events', description: '更新异常事件状态' },
  { functionId: 'safety_build_process_public', functionName: '楼宇门禁公示发布', modelId: 'build', description: '更新流程状态' },
  { functionId: 'safety_build_process_enter', functionName: '楼宇门禁入场', modelId: 'build', description: '记录入场信息' },
  { functionId: 'safety_build_process_discard', functionName: '楼宇门禁废弃', modelId: 'build', description: '更新流程状态为废弃' },
  { functionId: 'safety_build_process_after', functionName: '楼宇门禁退场', modelId: 'build', description: '记录退场信息' },
  { functionId: 'safety_park_power_process', functionName: '人车权限发布', modelId: 'park', description: '更新当前节点，准备下发权限' },
  { functionId: 'safety_park_power_process_publish', functionName: '人车权限下发', modelId: 'park', description: '异步下发人脸/卡/车权限' },
  { functionId: 'safety_park_power_process_discard', functionName: '人车权限废弃', modelId: 'park', description: '更新流程状态为废弃' },
  { functionId: 'safety_entry_blank_process_pass', functionName: '黑名单入场通过', modelId: 'entry_blank', description: '更新黑名单备案状态' },
  { functionId: 'safety_visitor_authorization_approval_pass', functionName: '访客授权通过', modelId: 'visitor', description: '更新访客授权状态' },
];

/**
 * BPM 审批操作类型 - 对齐蓝凌BPM的 operationType
 */
export const BPM_OPERATION_TYPES = [
  { value: 'handler_pass', label: '审批通过', desc: '推进到下一节点' },
  { value: 'handler_refuse', label: '驳回', desc: '驳回到上一节点' },
  { value: 'drafter_abandon', label: '废弃', desc: '创建人废弃流程' },
  { value: 'handler_assign', label: '转派', desc: '转派给其他处理人' },
  { value: 'handler_communicate', label: '传阅', desc: '传阅给其他用户' },
];

/**
 * BPM SOAP 接口配置 - 对齐蓝凌BPM的主动调用接口
 */
export const BPM_SOAP_METHODS = [
  { method: 'CreateProcess', label: '创建流程', desc: '创建BPM流程实例' },
  { method: 'CreateAndApproveProcess', label: '创建并提交', desc: '二合一，自动推进到下一节点' },
  { method: 'ApproveProcess', label: '审批流程', desc: '审批/驳回/废弃操作' },
  { method: 'GetOperationList', label: '获取操作列表', desc: '获取当前可用操作' },
  { method: 'GetCurrentNodesInfo', label: '获取当前节点', desc: '获取流程当前节点信息' },
  { method: 'GetApproverList', label: '获取审批人列表', desc: '获取节点审批人' },
  { method: 'passProcess', label: '传阅', desc: '传阅流程给其他用户' },
];

/** 系统标识 - 蓝凌BPM的 sysId */
export const BPM_SYS_ID = 'SAFETY';
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】对齐业务实际流程引擎配置（蓝凌LBPM 12个业务流程模型）---
