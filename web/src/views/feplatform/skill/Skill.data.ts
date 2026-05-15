import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Skill名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '编码',
    align: 'center',
    dataIndex: 'code',
  },
  {
    title: '分类',
    align: 'center',
    dataIndex: 'category',
    customRender: ({ text }) => {
      const map = { official: '官方', business: '业务', app: '应用级' };
      return map[text] || text;
    },
  },
  {
    title: '描述',
    align: 'left',
    dataIndex: 'description',
    ellipsis: true,
  },
  {
    title: '版本',
    align: 'center',
    dataIndex: 'version',
    width: 80,
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    customRender: ({ text }) => {
      return text === 'enable' ? '启用' : '禁用';
    },
    width: 80,
  },
  {
    title: '排序',
    align: 'center',
    dataIndex: 'sortNo',
    width: 80,
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
    label: '名称',
    field: 'name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '编码',
    field: 'code',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '分类',
    field: 'category',
    component: 'Select',
    componentProps: {
      options: [
        { label: '官方', value: 'official' },
        { label: '业务', value: 'business' },
        { label: '应用级', value: 'app' },
      ],
      placeholder: '请选择分类',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'Skill名称',
    field: 'name',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入Skill名称' }],
  },
  {
    label: 'Skill编码',
    field: 'code',
    component: 'Input',
    dynamicRules: () => [
      { required: true, message: '请输入Skill编码' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/, message: '编码必须以字母开头，可包含数字、下划线、横杠' },
    ],
  },
  {
    label: '分类',
    field: 'category',
    component: 'Select',
    componentProps: {
      options: [
        { label: '官方', value: 'official' },
        { label: '业务', value: 'business' },
        { label: '应用级', value: 'app' },
      ],
    },
    dynamicRules: () => [{ required: true, message: '请选择分类' }],
  },
  {
    label: '描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: { rows: 3 },
  },
  {
    label: 'Prompt模板',
    field: 'promptTemplate',
    component: 'InputTextArea',
    componentProps: { rows: 10, placeholder: '请输入Prompt模板内容...' },
    dynamicRules: () => [{ required: true, message: '请输入Prompt模板' }],
  },
  {
    label: '图标',
    field: 'icon',
    component: 'Input',
  },
  {
    label: '关联应用ID',
    field: 'appId',
    component: 'Input',
    ifShow: ({ values }) => values.category === 'app',
  },
  {
    label: '版本号',
    field: 'version',
    component: 'Input',
    defaultValue: '1.0.0',
  },
  {
    label: '状态',
    field: 'status',
    component: 'RadioGroup',
    defaultValue: 'enable',
    componentProps: {
      options: [
        { label: '启用', value: 'enable' },
        { label: '禁用', value: 'disable' },
      ],
    },
  },
  {
    label: '排序号',
    field: 'sortNo',
    component: 'InputNumber',
    defaultValue: 0,
  },
  {
    label: '输入参数Schema',
    field: 'inputSchema',
    component: 'InputTextArea',
    componentProps: { rows: 4, placeholder: 'JSON格式，如: {"type":"object","properties":{}}' },
  },
  {
    label: '输出参数Schema',
    field: 'outputSchema',
    component: 'InputTextArea',
    componentProps: { rows: 4, placeholder: 'JSON格式，如: {"type":"object","properties":{}}' },
  },
  {
    label: '使用示例',
    field: 'examples',
    component: 'InputTextArea',
    componentProps: { rows: 4, placeholder: 'JSON数组格式' },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

export const categoryMap = {
  official: { label: '官方', color: 'blue' },
  business: { label: '业务', color: 'green' },
  app: { label: '应用级', color: 'orange' },
};
