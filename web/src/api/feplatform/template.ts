import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  // 模版主表
  list = '/feplatform/template/list',
  queryById = '/feplatform/template/queryById',
  add = '/feplatform/template/add',
  edit = '/feplatform/template/edit',
  deleteOne = '/feplatform/template/delete',
  deleteBatch = '/feplatform/template/deleteBatch',
  importExcel = '/feplatform/template/importExcel',
  exportXls = '/feplatform/template/exportXls',

  // 模版版本
  versionList = '/feplatform/templateVersion/list',
  versionQueryById = '/feplatform/templateVersion/queryById',
  versionAdd = '/feplatform/templateVersion/add',
  versionEdit = '/feplatform/templateVersion/edit',
  versionDelete = '/feplatform/templateVersion/delete',
  versionDeleteBatch = '/feplatform/templateVersion/deleteBatch',

  // 应用创建记录
  recordList = '/feplatform/createRecord/list',
  recordQueryById = '/feplatform/createRecord/queryById',
  recordDelete = '/feplatform/createRecord/delete',
  recordDeleteBatch = '/feplatform/createRecord/deleteBatch',
  recordExportXls = '/feplatform/createRecord/exportXls',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

// ============ 模版主表 ============
export const list = (params) => defHttp.get({ url: Api.list, params });

export const queryById = (id: string) => defHttp.get({ url: Api.queryById, params: { id } });

export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.add;
  return defHttp.post({ url, params });
};

export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

// ============ 模版版本 ============
export const versionList = (params) => defHttp.get({ url: Api.versionList, params });

export const versionQueryById = (id: string) => defHttp.get({ url: Api.versionQueryById, params: { id } });

export const versionSaveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.versionEdit : Api.versionAdd;
  return defHttp.post({ url, params });
};

export const versionDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.versionDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const versionBatchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.versionDeleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

// ============ 应用创建记录 ============
export const recordList = (params) => defHttp.get({ url: Api.recordList, params });

export const recordQueryById = (id: string) => defHttp.get({ url: Api.recordQueryById, params: { id } });

export const recordDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.recordDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const recordBatchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.recordDeleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

export const getRecordExportUrl = Api.recordExportXls;