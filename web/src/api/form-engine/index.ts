import { defHttp } from '/@/utils/http/axios';

/**
 * 表单引擎与流程仿真测试工作台 -- API 契约
 * 说明：defHttp 会自动拼接 VITE_GLOB_API_URL（/jeecgboot），
 *      代理将 /jeecgboot/formengine/... 转发到后端 /jeecg-boot/formengine/...
 */
enum Api {
  PROCESS_LIST = '/formengine/process/list',
  PROCESS_ADD = '/formengine/process/add',
  PROCESS_EDIT = '/formengine/process/edit',
  PROCESS_DELETE = '/formengine/process/delete',
  PROCESS_BY_ID = '/formengine/process/queryById',
  TEMPLATE_LIST = '/formengine/template/list',
  FIELD_LIST = '/formengine/field/list',
  FIELD_VALUE_LIST = '/formengine/field/value/list',
  METHOD_LIST = '/formengine/method/list',
  METHOD_DO = '/formengine/method/do',
  PROCESS_IMPORT = '/formengine/process/import',
  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、设计器前置检查、初始化渲染接口---
  SCHEMA_LIST = '/formengine/schema/list',
  SCHEMA_SAVE_BATCH = '/formengine/schema/saveBatch',
  SCHEMA_DELETE = '/formengine/schema/delete',
  DESIGNER_CHECK = '/formengine/designer/check',
  DESIGNER_INIT = '/formengine/designer/init',
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、设计器前置检查、初始化渲染接口---
  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】新增BPM对接接口管理(查询/更新/同步)---
  BPMAPI_LIST = '/formengine/bpmApi/list',
  BPMAPI_UPDATE = '/formengine/bpmApi/update',
  BPMAPI_SYNC = '/formengine/bpmApi/sync',
  BPMAPI_GENERATE = '/formengine/bpmApi/generate',
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】新增BPM对接接口管理(查询/更新/同步)---
}

/** 流程列表 */
export const getProcessList = (params?) => defHttp.get({ url: Api.PROCESS_LIST, params });

/** 新建流程 */
export const addProcess = (params) => defHttp.post({ url: Api.PROCESS_ADD, params });

/** 编辑流程（保存表单 Schema 等） */
export const editProcess = (params) => defHttp.put({ url: Api.PROCESS_EDIT, params });

/** 删除流程 */
export const deleteProcess = (id) =>
  defHttp.delete({ url: Api.PROCESS_DELETE, params: { id } }, { joinParamsToUrl: true });

/** 根据 ID 查询流程（含 formSchema） */
export const getProcessById = (id) => defHttp.get({ url: Api.PROCESS_BY_ID, params: { id } });

/** 表单模板列表 */
export const getTemplateFormList = () => defHttp.get({ url: Api.TEMPLATE_LIST });

/** 表单字段列表（设计器字段绑定用） */
export const getFormFieldList = (templateId) => defHttp.get({ url: Api.FIELD_LIST, params: { templateId } });

/** 表单字段值 + 权限矩阵（仿真用，按角色返回） */
export const getFormFieldValueList = (templateId, role) =>
  defHttp.get({ url: Api.FIELD_VALUE_LIST, params: { templateId, role } });

/** 业务方法列表（动作绑定用） */
export const getMethodInfo = () => defHttp.get({ url: Api.METHOD_LIST });

/** 触发业务方法 */
export const doMethodProcess = (params) => defHttp.post({ url: Api.METHOD_DO, params });

/** 导入流程定义 */
export const importProcessDefinition = (params) => defHttp.post({ url: Api.PROCESS_IMPORT, params });

// update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、设计器前置检查、初始化渲染接口---

/** 流程级Schema字段-列表查询 */
export const getSchemaFields = (processId: string) =>
  defHttp.get({ url: Api.SCHEMA_LIST, params: { processId } });

/** 流程级Schema字段-批量保存 */
export const saveSchemaFields = (processId: string, fields: any[]) =>
  defHttp.post({ url: Api.SCHEMA_SAVE_BATCH, params: { processId, fields } });

/** 流程级Schema字段-删除 */
export const deleteSchemaFields = (processId: string) =>
  defHttp.delete({ url: Api.SCHEMA_DELETE, params: { processId } }, { joinParamsToUrl: true });

/** 设计器前置检查（检查是否已导入流程定义+配置Schema字段） */
export const checkDesignerPrecondition = (processId: string) =>
  defHttp.get({ url: Api.DESIGNER_CHECK, params: { processId } });

/** 设计器初始化渲染（根据Schema字段自动生成初始画布布局） */
export const initDesignerLayout = (processId: string) =>
  defHttp.get({ url: Api.DESIGNER_INIT, params: { processId } });
// update-end---author:formengine ---date:2026-07-15  for：【表单引擎】新增流程级Schema字段CRUD、设计器前置检查、初始化渲染接口---

// update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】新增BPM对接接口管理API---

/** BPM对接接口-列表查询（查询指定流程的7个BPM对接接口配置） */
export const getBpmApiList = (processId: string) =>
  defHttp.get({ url: Api.BPMAPI_LIST, params: { processId } });

/** BPM对接接口-更新URL配置 */
export const updateBpmApi = (params: { id: string; apiUrl: string }) =>
  defHttp.post({ url: Api.BPMAPI_UPDATE, params });

/** BPM对接接口-批量同步到BPM平台 */
export const syncBpmApis = (processId: string) =>
  defHttp.post({ url: Api.BPMAPI_SYNC, params: { processId } });

/** BPM对接接口-为流程补生成接口记录 */
export const generateBpmApis = (processId: string) =>
  defHttp.post({ url: Api.BPMAPI_GENERATE, params: { processId } });
// update-end---author:formengine ---date:2026-07-15  for：【表单引擎】新增BPM对接接口管理API---
