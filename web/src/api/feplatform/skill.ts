import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  list = '/feplatform/skill/list',
  queryById = '/feplatform/skill/queryById',
  add = '/feplatform/skill/add',
  edit = '/feplatform/skill/edit',
  deleteOne = '/feplatform/skill/delete',
  deleteBatch = '/feplatform/skill/deleteBatch',
  importExcel = '/feplatform/skill/importExcel',
  exportXls = '/feplatform/skill/exportXls',
  listByCategory = '/feplatform/skill/listByCategory',
  exportMcpConfig = '/feplatform/skill/exportMcpConfig',
  validateDependencies = '/feplatform/skill/validateDependencies',
  testSkill = '/feplatform/skill/testSkill',
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

export const listByCategory = (category: string) =>
  defHttp.get({ url: Api.listByCategory, params: { category } });

export const exportMcpConfig = (skillCodes: string) =>
  defHttp.get({ url: Api.exportMcpConfig, params: { skillCodes } });

export const validateDependencies = (skillIds: string[]) =>
  defHttp.post({ url: Api.validateDependencies, params: skillIds });

export const testSkill = (params) =>
  defHttp.post({ url: Api.testSkill, params });
