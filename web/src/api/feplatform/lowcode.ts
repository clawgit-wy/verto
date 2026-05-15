import { defHttp } from '/@/utils/http/axios';

enum Api {
  chat2code = '/feplatform/lowcode/chat2code',
  availableSkills = '/feplatform/lowcode/availableSkills',
}

export const chat2code = (params) => defHttp.post({ url: Api.chat2code, params });

export const getAvailableSkills = (appId?: string) =>
  defHttp.get({ url: Api.availableSkills, params: appId ? { appId } : {} });
