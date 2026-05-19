import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/lowcode/feProjectGeneration/list',
  delete = '/lowcode/feProjectGeneration/delete',
  deleteBatch = '/lowcode/feProjectGeneration/deleteBatch',
  queryById = '/lowcode/feProjectGeneration/queryById',
  exportXls = '/lowcode/feProjectGeneration/exportXls',
  importExcel = '/lowcode/feProjectGeneration/importExcel',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

export const list = (params) => defHttp.get({ url: Api.list, params });

export const queryById = (id) => defHttp.get({ url: Api.queryById, params: { id } }, { isTransformResponse: false });

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
