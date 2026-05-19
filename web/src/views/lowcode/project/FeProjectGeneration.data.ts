import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '项目名称',
    align: 'center',
    dataIndex: 'projectName',
  },
  {
    title: '项目编码',
    align: 'center',
    dataIndex: 'projectCode',
  },
  {
    title: '模板名称',
    align: 'center',
    dataIndex: 'templateName',
  },
  {
    title: '生成方式',
    align: 'center',
    dataIndex: 'generationType_dictText',
  },
  {
    title: 'GitLab地址',
    align: 'left',
    dataIndex: 'gitlabUrl',
    customRender: ({ text }) => {
      if (!text) return '-';
      return text;
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status_dictText',
  },
  {
    title: '生成人',
    align: 'center',
    dataIndex: 'generatedBy',
  },
  {
    title: '生成时间',
    align: 'center',
    dataIndex: 'generatedTime',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'projectName',
    label: '项目名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'generationType',
    label: '生成方式',
    component: 'Select',
    componentProps: {
      options: [
        { label: '推送到GitLab', value: 'gitlab' },
        { label: '下载到本地', value: 'download' },
      ],
      placeholder: '请选择生成方式',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '生成中', value: 'generating' },
        { label: '成功', value: 'success' },
        { label: '失败', value: 'failed' },
      ],
      placeholder: '请选择状态',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];
