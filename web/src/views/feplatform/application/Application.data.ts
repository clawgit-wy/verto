import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '应用名称',
    align: 'center',
    dataIndex: 'appName',
  },
  {
    title: '应用编码',
    align: 'center',
    dataIndex: 'appCode',
  },
  {
    title: '描述',
    align: 'left',
    dataIndex: 'description',
    ellipsis: true,
  },
  {
    title: '技术栈',
    align: 'center',
    dataIndex: 'techStack',
    customRender: ({ text }) => {
      if (!text) return '-';
      const stack = typeof text === 'string' ? JSON.parse(text) : text;
      return Object.keys(stack || {}).join(', ') || '-';
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    customRender: ({ text }) => {
      const map = { active: '活跃', archived: '归档', developing: '开发中' };
      return map[text] || text;
    },
    width: 100,
  },
  {
    title: '主分支',
    align: 'center',
    dataIndex: 'repoBranch',
    width: 100,
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'createBy',
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '应用名称',
    field: 'appName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '应用编码',
    field: 'appCode',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '状态',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: [
        { label: '活跃', value: 'active' },
        { label: '归档', value: 'archived' },
        { label: '开发中', value: 'developing' },
      ],
      placeholder: '请选择状态',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '应用名称',
    field: 'appName',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入应用名称' }],
  },
  {
    label: '应用编码',
    field: 'appCode',
    component: 'Input',
    dynamicRules: () => [
      { required: true, message: '请输入应用编码' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/, message: '编码必须以字母开头，可包含数字、下划线、横杠' },
    ],
  },
  {
    label: '应用描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: { rows: 3 },
  },
  {
    label: '应用图标',
    field: 'icon',
    component: 'Input',
    placeholder: '请输入图标URL',
  },
  {
    label: '技术栈',
    field: 'techStack',
    component: 'InputTextArea',
    componentProps: { rows: 3, placeholder: 'JSON格式，如: {"vue":"3.x","vite":"6.x"}' },
  },
  {
    label: '代码仓库地址',
    field: 'repoUrl',
    component: 'Input',
    placeholder: '如: https://gitlab.example.com/frontend/my-app',
  },
  {
    label: '主分支',
    field: 'repoBranch',
    component: 'Input',
    defaultValue: 'main',
  },
  {
    label: '部署地址',
    field: 'deployUrl',
    component: 'Input',
    placeholder: '如: https://my-app.example.com',
  },
  {
    label: '负责人ID',
    field: 'ownerId',
    component: 'Input',
  },
  {
    label: '所属团队ID',
    field: 'teamId',
    component: 'Input',
  },
  {
    label: '状态',
    field: 'status',
    component: 'RadioGroup',
    defaultValue: 'active',
    componentProps: {
      options: [
        { label: '活跃', value: 'active' },
        { label: '归档', value: 'archived' },
        { label: '开发中', value: 'developing' },
      ],
    },
  },
  {
    label: '应用级Prompt模板',
    field: 'promptTemplate',
    component: 'InputTextArea',
    componentProps: { rows: 6, placeholder: '为该应用设置专属的Prompt模板...' },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

export const statusMap = {
  active: { label: '活跃', color: 'green' },
  archived: { label: '归档', color: 'gray' },
  developing: { label: '开发中', color: 'blue' },
};