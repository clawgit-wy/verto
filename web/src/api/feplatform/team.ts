import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  list = '/feplatform/team/list',
  queryById = '/feplatform/team/queryById',
  add = '/feplatform/team/add',
  edit = '/feplatform/team/edit',
  deleteOne = '/feplatform/team/delete',
  deleteBatch = '/feplatform/team/deleteBatch',
  importExcel = '/feplatform/team/importExcel',
  exportXls = '/feplatform/team/exportXls',
}

export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

export const list = (params) => defHttp.get({ url: Api.list, params });

export const queryById = (id: string) => defHttp.get({ url: Api.queryById, params: { id } });

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

export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.add;
  return defHttp.post({ url, params });
};