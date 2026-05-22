import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

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
    title: '邮箱',
    align: 'center',
    dataIndex: 'email',
  },
  {
    title: '入职时间',
    align: 'center',
    dataIndex: 'hireDate',
  },
  {
    title: '工位位置',
    align: 'center',
    dataIndex: 'seatLocation',
  },
  {
    title: '擅长技能',
    align: 'center',
    dataIndex: 'skillTags_dictText',
    customRender: ({ text, record }) => {
      let tags: string[] = [];
      if (text) {
        tags = typeof text === 'string' ? text.split(',') : [text];
      } else if (record.skillTags) {
        const raw = record.skillTags;
        tags = typeof raw === 'string' ? JSON.parse(raw) : raw;
      }
      if (!tags || tags.length === 0) return '-';
      const colors = ['blue', 'green', 'orange', 'red', 'purple', 'cyan'];
      return h('span', {}, tags.map((tag, index) =>
        h(Tag, { color: colors[index % colors.length], style: 'margin-bottom: 2px;' }, () => tag)
      ));
    },
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status_dictText',
    customRender: ({ text, record }) => {
      const value = record.status;
      if (!value && !text) return '-';
      const label = text || (value === 'active' ? '在职' : '离职');
      const color = value === 'active' ? 'green' : 'red';
      return h(Tag, { color }, () => label);
    },
    width: 80,
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
    label: '状态',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_developer_status',
      placeholder: '请选择状态',
      allowClear: true,
    },
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
    label: '邮箱',
    field: 'email',
    component: 'Input',
    rules: [
      { type: 'email', message: '请输入有效的邮箱地址' },
    ],
  },
  {
    label: '入职时间',
    field: 'hireDate',
    component: 'DatePicker',
    componentProps: {
      placeholder: '请选择入职时间',
      valueFormat: 'YYYY-MM-DD',
      style: { width: '100%' },
    },
  },
  {
    label: '工位位置',
    field: 'seatLocation',
    component: 'Input',
  },
  {
    label: '擅长技能',
    field: 'skillTags',
    component: 'JSelectMultiple',
    componentProps: {
      dictCode: 'fe_skill_tag',
      placeholder: '请选择擅长技能',
    },
  },
  {
    label: '状态',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_developer_status',
      placeholder: '请选择状态',
    },
    defaultValue: 'active',
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      maxlength: 500,
      showCount: true,
      placeholder: '请输入备注，最多500字',
    },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

export const descSchema: DescItem[] = [
  {
    field: 'realName',
    label: '姓名',
  },
  {
    field: 'employeeNo',
    label: '工号',
  },
  {
    field: 'email',
    label: '邮箱',
  },
  {
    field: 'hireDate',
    label: '入职时间',
  },
  {
    field: 'seatLocation',
    label: '工位位置',
  },
  {
    field: 'skillTags_dictText',
    label: '擅长技能',
    render: (val, data) => {
      let tags: string[] = [];
      if (val) {
        tags = typeof val === 'string' ? val.split(',') : [val];
      } else if (data.skillTags) {
        const raw = data.skillTags;
        tags = typeof raw === 'string' ? JSON.parse(raw) : raw;
      }
      if (!tags || tags.length === 0) return '-';
      const colors = ['blue', 'green', 'orange', 'red', 'purple', 'cyan'];
      return h('span', {}, tags.map((tag, index) =>
        h(Tag, { color: colors[index % colors.length], style: 'margin-bottom: 2px;' }, () => tag)
      ));
    },
  },
  {
    field: 'status_dictText',
    label: '状态',
    render: (val, data) => {
      const value = data.status;
      if (!value && !val) return '-';
      const label = val || (value === 'active' ? '在职' : '离职');
      const color = value === 'active' ? 'green' : 'red';
      return h(Tag, { color }, () => label);
    },
  },
  {
    field: 'remark',
    label: '备注',
  },
];
