import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '团队名称',
    align: 'center',
    dataIndex: 'teamName',
  },
  {
    title: '团队编码',
    align: 'center',
    dataIndex: 'teamCode',
  },
  {
    title: '负责人',
    align: 'center',
    dataIndex: 'leaderName',
  },
  {
    title: '描述',
    align: 'left',
    dataIndex: 'description',
    ellipsis: true,
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
    label: '团队名称',
    field: 'teamName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '团队编码',
    field: 'teamCode',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '团队名称',
    field: 'teamName',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入团队名称' }],
  },
  {
    label: '团队编码',
    field: 'teamCode',
    component: 'Input',
    dynamicRules: () => [
      { required: true, message: '请输入团队编码' },
      { pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/, message: '编码必须以字母开头，可包含数字、下划线、横杠' },
    ],
  },
  {
    label: '负责人ID',
    field: 'leaderId',
    component: 'Input',
  },
  {
    label: '团队描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: { rows: 3 },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];