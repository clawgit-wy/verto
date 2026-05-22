import { BasicColumn, FormSchema } from '/@/components/Table';

// ============ 模版主表 ============
export const templateColumns: BasicColumn[] = [
  {
    title: '模版名称',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: '模版编码',
    align: 'center',
    dataIndex: 'code',
  },
  {
    title: '框架',
    align: 'center',
    dataIndex: 'framework',
  },
  {
    title: '框架版本',
    align: 'center',
    dataIndex: 'frameworkVersion',
    width: 100,
  },
  {
    title: '默认分支',
    align: 'center',
    dataIndex: 'branch',
    width: 100,
  },
  {
    title: 'GitLab地址',
    align: 'left',
    dataIndex: 'gitlabUrl',
    ellipsis: true,
  },
  {
    title: '可见性',
    align: 'center',
    dataIndex: 'visibility',
    width: 90,
    customRender: ({ text }) => (text === 'public' ? '公开' : '私有'),
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    width: 90,
    customRender: ({ text }) => (text === 'enable' ? '上架' : '下架'),
  },
  {
    title: '排序',
    align: 'center',
    dataIndex: 'sortNo',
    width: 70,
  },
  {
    title: '创建时间',
    align: 'center',
    dataIndex: 'createTime',
    width: 170,
  },
];

export const templateSearchSchema: FormSchema[] = [
  { label: '模版名称', field: 'name', component: 'Input', colProps: { span: 6 } },
  { label: '模版编码', field: 'code', component: 'Input', colProps: { span: 6 } },
  {
    label: '状态',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: [
        { label: '上架', value: 'enable' },
        { label: '下架', value: 'disable' },
      ],
      placeholder: '请选择状态',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const templateFormSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  {
    label: '模版名称',
    field: 'name',
    component: 'Input',
    required: true,
  },
  {
    label: '模版编码',
    field: 'code',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '唯一标识，如：vue3-admin' },
  },
  {
    label: 'GitLab地址',
    field: 'gitlabUrl',
    component: 'Input',
    required: true,
    componentProps: { placeholder: 'https://gitlab.example.com/group/repo.git' },
  },
  {
    label: '默认分支',
    field: 'branch',
    component: 'Input',
    defaultValue: 'main',
  },
  {
    label: '框架',
    field: 'framework',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Vue2', value: 'Vue2' },
        { label: 'Vue3', value: 'Vue3' },
        { label: 'React', value: 'React' },
        { label: 'Angular', value: 'Angular' },
        { label: 'jQuery', value: 'jQuery' },
      ],
    },
  },
  { label: '框架版本', field: 'frameworkVersion', component: 'Input' },
  {
    label: 'Node版本范围',
    field: 'nodeVersionRange',
    component: 'Input',
    componentProps: { placeholder: '如：v16-v18' },
  },
  {
    label: '可见性',
    field: 'visibility',
    component: 'RadioGroup',
    defaultValue: 'public',
    componentProps: {
      options: [
        { label: '公开', value: 'public' },
        { label: '私有', value: 'private' },
      ],
    },
  },
  {
    label: '状态',
    field: 'status',
    component: 'RadioGroup',
    defaultValue: 'enable',
    componentProps: {
      options: [
        { label: '上架', value: 'enable' },
        { label: '下架', value: 'disable' },
      ],
    },
  },
  {
    label: '排序号',
    field: 'sortNo',
    component: 'InputNumber',
    defaultValue: 0,
  },
  { label: '预览图', field: 'previewImage', component: 'Input' },
  {
    label: '模版描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: { rows: 3 },
  },
];

// ============ 模版版本 ============
export const versionColumns: BasicColumn[] = [
  { title: '模版ID', align: 'center', dataIndex: 'templateId' },
  { title: '版本号', align: 'center', dataIndex: 'version' },
  { title: 'Git标签', align: 'center', dataIndex: 'gitTag' },
  { title: 'Commit SHA', align: 'center', dataIndex: 'commitSha', ellipsis: true },
  { title: '框架', align: 'center', dataIndex: 'framework' },
  { title: '框架版本', align: 'center', dataIndex: 'frameworkVersion' },
  { title: '变更说明', align: 'left', dataIndex: 'changelog', ellipsis: true },
  { title: '创建时间', align: 'center', dataIndex: 'createTime', width: 170 },
];

export const versionSearchSchema: FormSchema[] = [
  { label: '模版ID', field: 'templateId', component: 'Input', colProps: { span: 6 } },
  { label: '版本号', field: 'version', component: 'Input', colProps: { span: 6 } },
];

export const versionFormSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  { label: '模版ID', field: 'templateId', component: 'Input', required: true },
  { label: '版本号', field: 'version', component: 'Input', required: true },
  { label: 'Git标签', field: 'gitTag', component: 'Input' },
  { label: 'Commit SHA', field: 'commitSha', component: 'Input' },
  { label: '框架', field: 'framework', component: 'Input' },
  { label: '框架版本', field: 'frameworkVersion', component: 'Input' },
  {
    label: '变更说明',
    field: 'changelog',
    component: 'InputTextArea',
    componentProps: { rows: 4 },
  },
];

// ============ 应用创建记录 ============
export const recordColumns: BasicColumn[] = [
  { title: '应用名称', align: 'center', dataIndex: 'appName' },
  { title: '应用编码', align: 'center', dataIndex: 'appCode' },
  { title: '模版ID', align: 'center', dataIndex: 'templateId' },
  { title: '版本ID', align: 'center', dataIndex: 'versionId' },
  {
    title: '输出类型',
    align: 'center',
    dataIndex: 'outputType',
    width: 100,
    customRender: ({ text }) => (text === 'gitlab' ? '创建GitLab仓库' : '下载ZIP'),
  },
  { title: 'GitLab地址', align: 'left', dataIndex: 'gitlabUrl', ellipsis: true },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    width: 100,
  },
  { title: '错误信息', align: 'left', dataIndex: 'errorMessage', ellipsis: true },
  { title: '创建时间', align: 'center', dataIndex: 'createTime', width: 170 },
];

export const recordSearchSchema: FormSchema[] = [
  { label: '应用名称', field: 'appName', component: 'Input', colProps: { span: 6 } },
  { label: '应用编码', field: 'appCode', component: 'Input', colProps: { span: 6 } },
  {
    label: '状态',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: [
        { label: '生成中', value: 'generating' },
        { label: '成功', value: 'success' },
        { label: '失败', value: 'failed' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];