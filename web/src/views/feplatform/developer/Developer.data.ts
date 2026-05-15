import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '姓名',
    align: 'center',
    dataIndex: 'realName',
  },
  {
    title: '工号',
    align: 'center',
    dataIndex: 'employeeNo',
  },
  {
    title: '所属团队',
    align: 'center',
    dataIndex: 'teamName',
  },
  {
    title: '角色',
    align: 'center',
    dataIndex: 'role',
    customRender: ({ text }) => {
      const map = { developer: '开发人员', lead: '技术负责人', manager: '项目经理' };
      return map[text] || text;
    },
    width: 100,
  },
  {
    title: '技能标签',
    align: 'center',
    dataIndex: 'skillTags',
    customRender: ({ text }) => {
      if (!text) return '-';
      const tags = typeof text === 'string' ? JSON.parse(text) : text;
      return tags.join(', ') || '-';
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    customRender: ({ text }) => {
      return text === 'active' ? '在职' : '离职';
    },
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
    label: '姓名',
    field: 'realName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '工号',
    field: 'employeeNo',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '团队ID',
    field: 'teamId',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '姓名',
    field: 'realName',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入姓名' }],
  },
  {
    label: '工号',
    field: 'employeeNo',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入工号' }],
  },
  {
    label: '系统用户ID',
    field: 'userId',
    component: 'Input',
  },
  {
    label: '所属团队ID',
    field: 'teamId',
    component: 'Input',
  },
  {
    label: '角色',
    field: 'role',
    component: 'Select',
    defaultValue: 'developer',
    componentProps: {
      options: [
        { label: '开发人员', value: 'developer' },
        { label: '技术负责人', value: 'lead' },
        { label: '项目经理', value: 'manager' },
      ],
    },
  },
  {
    label: '技能标签',
    field: 'skillTags',
    component: 'Input',
    placeholder: '多个标签用逗号分隔',
  },
  {
    label: '状态',
    field: 'status',
    component: 'RadioGroup',
    defaultValue: 'active',
    componentProps: {
      options: [
        { label: '在职', value: 'active' },
        { label: '离职', value: 'inactive' },
      ],
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

export const roleMap = {
  developer: { label: '开发人员', color: 'blue' },
  lead: { label: '技术负责人', color: 'orange' },
  manager: { label: '项目经理', color: 'red' },
};