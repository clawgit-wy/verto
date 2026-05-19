import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '模板名称',
    align: 'center',
    dataIndex: 'templateName',
  },
  {
    title: '模板编码',
    align: 'center',
    dataIndex: 'templateCode',
  },
  {
    title: '模板类型',
    align: 'center',
    dataIndex: 'templateType_dictText',
  },
  {
    title: '前端框架',
    align: 'center',
    dataIndex: 'framework',
  },
  {
    title: '构建工具',
    align: 'center',
    dataIndex: 'buildTool',
  },
  {
    title: 'UI组件库',
    align: 'center',
    dataIndex: 'uiLibrary',
  },
  {
    title: '开发语言',
    align: 'center',
    dataIndex: 'language',
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status_dictText',
  },
  {
    title: '排序',
    align: 'center',
    dataIndex: 'sortNo',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'templateName',
    label: '模板名称',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'templateType',
    label: '模板类型',
    component: 'Select',
    componentProps: {
      options: [
        { label: '移动端', value: 'mobile' },
        { label: 'PC端', value: 'pc' },
        { label: 'H5', value: 'h5' },
      ],
      placeholder: '请选择模板类型',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    field: 'framework',
    label: '前端框架',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Vue3', value: 'Vue3' },
        { label: 'React', value: 'React' },
      ],
      placeholder: '请选择前端框架',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'templateName',
    label: '模板名称',
    component: 'Input',
    required: true,
  },
  {
    field: 'templateCode',
    label: '模板编码',
    component: 'Input',
    required: true,
  },
  {
    field: 'templateType',
    label: '模板类型',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: '移动端', value: 'mobile' },
        { label: 'PC端', value: 'pc' },
        { label: 'H5', value: 'h5' },
      ],
    },
  },
  {
    field: 'framework',
    label: '前端框架',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: 'Vue3', value: 'Vue3' },
        { label: 'React', value: 'React' },
        { label: 'Angular', value: 'Angular' },
      ],
    },
  },
  {
    field: 'buildTool',
    label: '构建工具',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: 'Vite', value: 'Vite' },
        { label: 'Webpack', value: 'Webpack' },
      ],
    },
  },
  {
    field: 'uiLibrary',
    label: 'UI组件库',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Ant Design Vue', value: 'AntDesignVue' },
        { label: 'Element Plus', value: 'ElementPlus' },
        { label: 'Vant', value: 'Vant' },
      ],
    },
  },
  {
    field: 'language',
    label: '开发语言',
    component: 'Select',
    defaultValue: 'TypeScript',
    componentProps: {
      options: [
        { label: 'TypeScript', value: 'TypeScript' },
        { label: 'JavaScript', value: 'JavaScript' },
      ],
    },
  },
  {
    field: 'description',
    label: '模板描述',
    component: 'InputTextArea',
    componentProps: {
      rows: 3,
    },
  },
  {
    field: 'modules',
    label: '模块配置',
    component: 'InputTextArea',
    componentProps: {
      rows: 6,
      placeholder: '请输入JSON格式的模块配置',
    },
  },
  {
    field: 'techStack',
    label: '技术栈配置',
    component: 'InputTextArea',
    componentProps: {
      rows: 6,
      placeholder: '请输入JSON格式的技术栈配置',
    },
  },
  {
    field: 'gitlabConfig',
    label: 'GitLab配置',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入JSON格式的GitLab配置',
    },
  },
  {
    field: 'ciCdConfig',
    label: 'CI/CD配置',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: '请输入JSON格式的CI/CD配置',
    },
  },
  {
    field: 'previewImage',
    label: '模板预览图',
    component: 'Input',
    componentProps: {
      placeholder: '预览图URL',
    },
  },
  {
    field: 'isDefault',
    label: '是否默认模板',
    component: 'Switch',
    defaultValue: '0',
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    defaultValue: 'enable',
    componentProps: {
      options: [
        { label: '启用', value: 'enable' },
        { label: '禁用', value: 'disable' },
      ],
    },
  },
  {
    field: 'sortNo',
    label: '排序号',
    component: 'InputNumber',
    defaultValue: 0,
  },
];
