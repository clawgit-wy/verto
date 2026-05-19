import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/lowcode/feTemplate/list',
  save = '/lowcode/feTemplate/save',
  add = '/lowcode/feTemplate/add',
  edit = '/lowcode/feTemplate/edit',
  delete = '/lowcode/feTemplate/delete',
  deleteBatch = '/lowcode/feTemplate/deleteBatch',
  exportXls = '/lowcode/feTemplate/exportXls',
  importExcel = '/lowcode/feTemplate/importExcel',
  queryById = '/lowcode/feTemplate/queryById',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

export const list = (params) => defHttp.get({ url: Api.list, params });

export const queryById = (id) => defHttp.get({ url: Api.queryById, params: { id } }, { isTransformResponse: false });

export const add = (params) => defHttp.post({ url: Api.add, data: params });

export const edit = (params) => defHttp.put({ url: Api.edit, data: params });

export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const deleteBatch = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
