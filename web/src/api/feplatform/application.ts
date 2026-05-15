import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  list = '/feplatform/application/list',
  queryById = '/feplatform/application/queryById',
  add = '/feplatform/application/add',
  edit = '/feplatform/application/edit',
  deleteOne = '/feplatform/application/delete',
  deleteBatch = '/feplatform/application/deleteBatch',
  importExcel = '/feplatform/application/importExcel',
  exportXls = '/feplatform/application/exportXls',
  skillList = '/feplatform/application/skillList',
  bindSkills = '/feplatform/application/bindSkills',
  unbindSkills = '/feplatform/application/unbindSkills',
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

export const getSkillList = (appId: string) =>
  defHttp.get({ url: Api.skillList, params: { appId } });

export const bindSkills = (appId: string, skillIds: string[]) =>
  defHttp.post({ url: Api.bindSkills, params: skillIds }, { joinParamsToUrl: true, params: { appId } });

export const unbindSkills = (appId: string, skillIds: string[]) =>
  defHttp.post({ url: Api.unbindSkills, params: skillIds }, { joinParamsToUrl: true, params: { appId } });
