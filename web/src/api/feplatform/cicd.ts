import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  // Jenkins实例
  jenkinsList = '/feplatform/cicd/jenkins/list',
  jenkinsQueryById = '/feplatform/cicd/jenkins/queryById',
  jenkinsAdd = '/feplatform/cicd/jenkins/add',
  jenkinsEdit = '/feplatform/cicd/jenkins/edit',
  jenkinsDelete = '/feplatform/cicd/jenkins/delete',
  jenkinsDeleteBatch = '/feplatform/cicd/jenkins/deleteBatch',
  jenkinsTest = '/feplatform/cicd/jenkins/testConnection',

  // 技术栈
  techStackList = '/feplatform/cicd/techStack/list',
  techStackQueryById = '/feplatform/cicd/techStack/queryById',
  techStackAdd = '/feplatform/cicd/techStack/add',
  techStackEdit = '/feplatform/cicd/techStack/edit',
  techStackDelete = '/feplatform/cicd/techStack/delete',
  techStackDeleteBatch = '/feplatform/cicd/techStack/deleteBatch',

  // Node版本
  nodeVersionList = '/feplatform/cicd/nodeVersion/list',
  nodeVersionQueryById = '/feplatform/cicd/nodeVersion/queryById',
  nodeVersionAdd = '/feplatform/cicd/nodeVersion/add',
  nodeVersionEdit = '/feplatform/cicd/nodeVersion/edit',
  nodeVersionDelete = '/feplatform/cicd/nodeVersion/delete',
  nodeVersionDeleteBatch = '/feplatform/cicd/nodeVersion/deleteBatch',

  // 流水线
  pipelineList = '/feplatform/cicd/pipeline/list',
  pipelineQueryById = '/feplatform/cicd/pipeline/queryById',
  pipelineAdd = '/feplatform/cicd/pipeline/add',
  pipelineEdit = '/feplatform/cicd/pipeline/edit',
  pipelineDelete = '/feplatform/cicd/pipeline/delete',
  pipelineDeleteBatch = '/feplatform/cicd/pipeline/deleteBatch',
  pipelineTrigger = '/feplatform/cicd/pipeline/triggerBuild',
  pipelineAbort = '/feplatform/cicd/pipeline/abortBuild',
  pipelineSync = '/feplatform/cicd/pipeline/syncBuilds',

  // 构建记录
  buildList = '/feplatform/cicd/build/list',
  buildQueryById = '/feplatform/cicd/build/queryById',
  buildDelete = '/feplatform/cicd/build/delete',
  buildDeleteBatch = '/feplatform/cicd/build/deleteBatch',
  buildConsoleLog = '/feplatform/cicd/build/consoleLog',
}

// ============ 通用批量删除 ============
function makeBatchDelete(url: string) {
  return (params, handleSuccess) => {
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '是否删除选中数据',
      okText: '确认',
      cancelText: '取消',
      onOk: () => {
        return defHttp.delete({ url, data: params }, { joinParamsToUrl: true }).then(() => {
          handleSuccess();
        });
      },
    });
  };
}

// ============ Jenkins实例 ============
export const jenkinsList = (params) => defHttp.get({ url: Api.jenkinsList, params });

export const jenkinsQueryById = (id: string) => defHttp.get({ url: Api.jenkinsQueryById, params: { id } });

export const jenkinsSaveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.jenkinsEdit : Api.jenkinsAdd;
  return defHttp.post({ url, params });
};

export const jenkinsDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.jenkinsDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const jenkinsBatchDelete = makeBatchDelete(Api.jenkinsDeleteBatch);

export const jenkinsTestConnection = (id: string) =>
  defHttp.get({ url: Api.jenkinsTest, params: { id } });

// ============ 技术栈 ============
export const techStackList = (params) => defHttp.get({ url: Api.techStackList, params });

export const techStackQueryById = (id: string) => defHttp.get({ url: Api.techStackQueryById, params: { id } });

export const techStackSaveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.techStackEdit : Api.techStackAdd;
  return defHttp.post({ url, params });
};

export const techStackDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.techStackDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const techStackBatchDelete = makeBatchDelete(Api.techStackDeleteBatch);

// ============ Node版本 ============
export const nodeVersionList = (params) => defHttp.get({ url: Api.nodeVersionList, params });

export const nodeVersionQueryById = (id: string) => defHttp.get({ url: Api.nodeVersionQueryById, params: { id } });

export const nodeVersionSaveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.nodeVersionEdit : Api.nodeVersionAdd;
  return defHttp.post({ url, params });
};

export const nodeVersionDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.nodeVersionDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const nodeVersionBatchDelete = makeBatchDelete(Api.nodeVersionDeleteBatch);

// ============ 流水线 ============
export const pipelineList = (params) => defHttp.get({ url: Api.pipelineList, params });

export const pipelineQueryById = (id: string) => defHttp.get({ url: Api.pipelineQueryById, params: { id } });

export const pipelineSaveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.pipelineEdit : Api.pipelineAdd;
  return defHttp.post({ url, params });
};

export const pipelineDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.pipelineDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const pipelineBatchDelete = makeBatchDelete(Api.pipelineDeleteBatch);

export const pipelineTriggerBuild = (pipelineId: string, parameters?: Record<string, string>) =>
  defHttp.post({
    url: `${Api.pipelineTrigger}?pipelineId=${encodeURIComponent(pipelineId)}`,
    data: parameters || {},
  });

export const pipelineAbortBuild = (pipelineId: string, buildNo: number) =>
  defHttp.post({ url: Api.pipelineAbort, params: { pipelineId, buildNo } }, { joinParamsToUrl: true });

export const pipelineSyncBuilds = (pipelineId: string) =>
  defHttp.post({ url: Api.pipelineSync, params: { pipelineId } }, { joinParamsToUrl: true });

export const buildConsoleLog = (id: string, start = 0) =>
  defHttp.get({ url: Api.buildConsoleLog, params: { id, start } });

// ============ 构建记录 ============
export const buildList = (params) => defHttp.get({ url: Api.buildList, params });

export const buildQueryById = (id: string) => defHttp.get({ url: Api.buildQueryById, params: { id } });

export const buildDelete = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.buildDelete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

export const buildBatchDelete = makeBatchDelete(Api.buildDeleteBatch);