import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { DescItem } from '/@/components/Description';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { list as developerList } from '/@/api/feplatform/developer';

const loadDeveloperOptions = async () => {
  const res = await developerList({ pageNo: 1, pageSize: 1000 });
  const records = Array.isArray(res) ? res : res?.records || [];
  return records.map((item) => ({
    label: item.realName,
    value: item.id,
  }));
};

export const columns: BasicColumn[] = [
  {
    title: '应用名称',
    align: 'center',
    dataIndex: 'appName',
  },
  {
    title: '应用简称',
    align: 'center',
    dataIndex: 'appShortName',
  },
  {
    title: '应用编码',
    align: 'center',
    dataIndex: 'appCode',
  },
  {
    title: '所属领域',
    align: 'center',
    dataIndex: 'domain_dictText',
    customRender: ({ text, record }) => {
      const value = record.domain;
      if (!value && !text) return '-';
      const label = text || value;
      return h(Tag, { color: 'blue' }, () => label);
    },
  },
  {
    title: '应用等级',
    align: 'center',
    dataIndex: 'appLevel_dictText',
    customRender: ({ text, record }) => {
      const value = record.appLevel;
      if (!value && !text) return '-';
      const label = text || value;
      const colorMap: Record<string, string> = {
        '1': 'red',
        '2': 'orange',
        '3': 'green',
        S: 'red',
        A: 'orange',
        B: 'green',
        C: 'blue',
      };
      const color = colorMap[value] || 'blue';
      return h(Tag, { color }, () => label);
    },
  },
  {
    title: '创建人',
    align: 'center',
    dataIndex: 'creatorId_dictText',
  },
  {
    title: '应用负责人',
    align: 'center',
    dataIndex: 'ownerId_dictText',
  },
  {
    title: 'Git仓库',
    align: 'center',
    dataIndex: 'repoUrl',
    customRender: ({ text }) => {
      if (!text) return '-';
      return h('a', { href: text, target: '_blank', style: 'color: #1890ff;' }, text);
    },
    ellipsis: true,
  },
  {
    title: '描述',
    align: 'left',
    dataIndex: 'description',
    ellipsis: true,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '应用简称',
    field: 'appShortName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '所属领域',
    field: 'domain',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_app_domain',
      placeholder: '请选择所属领域',
      allowClear: true,
    },
    colProps: { span: 6 },
  },
  {
    label: '应用等级',
    field: 'appLevel',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_app_level',
      placeholder: '请选择应用等级',
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
    label: '应用简称',
    field: 'appShortName',
    component: 'Input',
    dynamicRules: () => [{ required: true, message: '请输入应用简称' }],
  },
  {
    label: '应用编码',
    field: 'appCode',
    component: 'Input',
    componentProps: ({ formModel }) => ({
      placeholder: '留空将自动生成',
      disabled: !!formModel?.id,
    }),
    helpMessage: ['新增时留空将自动生成；保存后不可修改'],
    rules: [
      { pattern: /^[A-Za-z0-9_-]+$/, message: '仅支持字母、数字、下划线和连字符' },
      { max: 50, message: '长度不能超过50个字符' },
    ],
  },
  {
    label: '所属领域',
    field: 'domain',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_app_domain',
      placeholder: '请选择所属领域',
    },
    dynamicRules: () => [{ required: true, message: '请选择所属领域' }],
  },
  {
    label: '应用等级',
    field: 'appLevel',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'fe_app_level',
      placeholder: '请选择应用等级',
    },
    dynamicRules: () => [{ required: true, message: '请选择应用等级' }],
  },
  {
    label: '创建人',
    field: 'creatorId',
    component: 'ApiSelect',
    componentProps: {
      api: loadDeveloperOptions,
      placeholder: '请选择创建人',
      showSearch: true,
      allowClear: true,
    },
  },
  {
    label: '应用负责人',
    field: 'ownerId',
    component: 'ApiSelect',
    componentProps: {
      api: loadDeveloperOptions,
      placeholder: '请选择应用负责人',
      showSearch: true,
      allowClear: true,
    },
  },
  {
    label: 'Git仓库',
    field: 'repoUrl',
    component: 'Input',
    componentProps: {
      placeholder: '请输入Git仓库地址',
    },
    rules: [
      { type: 'url', message: '请输入有效的URL地址' },
    ],
  },
  {
    label: '描述',
    field: 'description',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      maxlength: 500,
      showCount: true,
      placeholder: '请输入描述，最多500字',
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
    field: 'appName',
    label: '应用名称',
  },
  {
    field: 'appShortName',
    label: '应用简称',
  },
  {
    field: 'appCode',
    label: '应用编码',
  },
  {
    field: 'domain_dictText',
    label: '所属领域',
    render: (val, data) => {
      const value = data.domain;
      if (!value && !val) return '-';
      return h(Tag, { color: 'blue' }, () => val || value);
    },
  },
  {
    field: 'appLevel_dictText',
    label: '应用等级',
    render: (val, data) => {
      const value = data.appLevel;
      if (!value && !val) return '-';
      const colorMap: Record<string, string> = {
        '1': 'red',
        '2': 'orange',
        '3': 'green',
        S: 'red',
        A: 'orange',
        B: 'green',
        C: 'blue',
      };
      const color = colorMap[value] || 'blue';
      return h(Tag, { color }, () => val || value);
    },
  },
  {
    field: 'creatorId_dictText',
    label: '创建人',
    render: (val, data) => val || data.creatorId || '-',
  },
  {
    field: 'ownerId_dictText',
    label: '应用负责人',
    render: (val, data) => val || data.ownerId || '-',
  },
  {
    field: 'repoUrl',
    label: 'Git仓库',
    render: (val) => {
      if (!val) return '-';
      return h('a', { href: val, target: '_blank', style: 'color: #1890ff;' }, val);
    },
  },
  {
    field: 'description',
    label: '描述',
  },
];
