import { BasicColumn, FormSchema } from '/@/components/Table';

// ============ Jenkins实例 ============
export const jenkinsColumns: BasicColumn[] = [
  { title: '实例名称', align: 'center', dataIndex: 'name' },
  { title: 'Jenkins地址', align: 'left', dataIndex: 'url', ellipsis: true },
  { title: '域', align: 'center', dataIndex: 'domain' },
  {
    title: '环境类型',
    align: 'center',
    dataIndex: 'envType',
    width: 100,
    customRender: ({ text }) => (text === 'prod' ? '生产环境' : '测试环境'),
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    width: 90,
    customRender: ({ text }) => (text === 'enable' ? '启用' : '停用'),
  },
  { title: '描述', align: 'left', dataIndex: 'description', ellipsis: true },
  { title: '创建时间', align: 'center', dataIndex: 'createTime', width: 170 },
];

export const jenkinsSearchSchema: FormSchema[] = [
  { label: '实例名称', field: 'name', component: 'Input', colProps: { span: 6 } },
  { label: 'Jenkins地址', field: 'url', component: 'Input', colProps: { span: 6 } },
  {
    label: '环境类型',
    field: 'envType',
    component: 'Select',
    componentProps: {
      options: [
        { label: '测试环境', value: 'test' },
        { label: '生产环境', value: 'prod' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const jenkinsFormSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  { label: '实例名称', field: 'name', component: 'Input', required: true },
  {
    label: 'Jenkins地址',
    field: 'url',
    component: 'Input',
    required: true,
    componentProps: { placeholder: 'http://jenkins.example.com' },
  },
  {
    label: '访问Token',
    field: 'token',
    component: 'InputPassword',
    componentProps: { placeholder: '建议使用加密Token' },
  },
  { label: '域', field: 'domain', component: 'Input' },
  {
    label: '环境类型',
    field: 'envType',
    component: 'RadioGroup',
    defaultValue: 'test',
    componentProps: {
      options: [
        { label: '测试环境', value: 'test' },
        { label: '生产环境', value: 'prod' },
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
        { label: '启用', value: 'enable' },
        { label: '停用', value: 'disable' },
      ],
    },
  },
  {
    label: '描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: { rows: 3 },
  },
];

// ============ 流水线 ============
export const pipelineColumns: BasicColumn[] = [
  { title: 'Job名称', align: 'center', dataIndex: 'jobName' },
  { title: '应用ID', align: 'center', dataIndex: 'appId' },
  { title: 'Jenkins实例ID', align: 'center', dataIndex: 'jenkinsId' },
  {
    title: '环境',
    align: 'center',
    dataIndex: 'env',
    width: 90,
    customRender: ({ text }) => {
      const map: Record<string, string> = { dev: '开发', test: '测试', prod: '生产' };
      return map[text] || text;
    },
  },
  {
    title: '检查级别',
    align: 'center',
    dataIndex: 'checkLevel',
    width: 100,
    customRender: ({ text }) => {
      const map: Record<string, string> = { strict: '严格', standard: '标准', loose: '宽松' };
      return map[text] || text;
    },
  },
  {
    title: '部署策略',
    align: 'center',
    dataIndex: 'deployStrategy',
    width: 130,
    customRender: ({ text }) => {
      const map: Record<string, string> = {
        auto_deploy: '自动部署',
        artifact_only: '仅制品库',
        online_deploy: '在线部署',
      };
      return map[text] || text;
    },
  },
  { title: '创建时间', align: 'center', dataIndex: 'createTime', width: 170 },
];

export const pipelineSearchSchema: FormSchema[] = [
  { label: 'Job名称', field: 'jobName', component: 'Input', colProps: { span: 6 } },
  { label: '应用ID', field: 'appId', component: 'Input', colProps: { span: 6 } },
  {
    label: '环境',
    field: 'env',
    component: 'Select',
    componentProps: {
      options: [
        { label: '开发', value: 'dev' },
        { label: '测试', value: 'test' },
        { label: '生产', value: 'prod' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
];

export const pipelineFormSchema: FormSchema[] = [
  { label: '', field: 'id', component: 'Input', show: false },
  { label: '关联应用ID', field: 'appId', component: 'Input', required: true },
  { label: 'Jenkins实例ID', field: 'jenkinsId', component: 'Input', required: true },
  { label: 'Job名称', field: 'jobName', component: 'Input', required: true },
  {
    label: '环境',
    field: 'env',
    component: 'Select',
    defaultValue: 'dev',
    componentProps: {
      options: [
        { label: '开发', value: 'dev' },
        { label: '测试', value: 'test' },
        { label: '生产', value: 'prod' },
      ],
    },
  },
  { label: '技术栈ID', field: 'techStackId', component: 'Input' },
  {
    label: '检查级别',
    field: 'checkLevel',
    component: 'Select',
    defaultValue: 'standard',
    componentProps: {
      options: [
        { label: '严格', value: 'strict' },
        { label: '标准', value: 'standard' },
        { label: '宽松', value: 'loose' },
      ],
    },
  },
  {
    label: '部署策略',
    field: 'deployStrategy',
    component: 'Select',
    defaultValue: 'artifact_only',
    componentProps: {
      options: [
        { label: '自动部署', value: 'auto_deploy' },
        { label: '仅制品库', value: 'artifact_only' },
        { label: '在线部署', value: 'online_deploy' },
      ],
    },
  },
  { label: '模板ID', field: 'templateId', component: 'Input' },
  { label: '模板版本ID', field: 'templateVersionId', component: 'Input' },
  {
    label: 'Jenkinsfile',
    field: 'jenkinsfile',
    component: 'InputTextArea',
    componentProps: { rows: 8 },
  },
];

// ============ 构建记录 ============
export const buildColumns: BasicColumn[] = [
  { title: '流水线ID', align: 'center', dataIndex: 'pipelineId' },
  { title: '构建号', align: 'center', dataIndex: 'buildNo', width: 90 },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    width: 100,
  },
  {
    title: '耗时(ms)',
    align: 'center',
    dataIndex: 'duration',
    width: 100,
  },
  { title: '触发用户', align: 'center', dataIndex: 'triggerUser' },
  { title: '提交SHA', align: 'center', dataIndex: 'commitSha', ellipsis: true },
  { title: '技术栈', align: 'center', dataIndex: 'techStack' },
  { title: '制品版本', align: 'center', dataIndex: 'artifactVersion' },
  { title: '质量得分', align: 'center', dataIndex: 'qualityScore', width: 100 },
  { title: '完成时间', align: 'center', dataIndex: 'finishTime', width: 170 },
];

export const buildSearchSchema: FormSchema[] = [
  { label: '流水线ID', field: 'pipelineId', component: 'Input', colProps: { span: 6 } },
  {
    label: '状态',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: [
        { label: '成功', value: 'success' },
        { label: '失败', value: 'failed' },
        { label: '运行中', value: 'running' },
        { label: '已取消', value: 'aborted' },
      ],
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  { label: '触发用户', field: 'triggerUser', component: 'Input', colProps: { span: 6 } },
];