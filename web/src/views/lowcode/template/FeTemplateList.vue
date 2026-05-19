<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate" v-auth="'lowcode:fe_template:add'"> 新增模板</a-button>
        <a-button preIcon="ant-design:download-outlined" @click="handleExportXls('前端代码模板')" v-auth="'lowcode:fe_template:exportXls'"> 导出</a-button>
        <j-upload-button preIcon="ant-design:import-outlined" text="导入" v-auth="'lowcode:fe_template:importExcel'"></j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <FeTemplateModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="fe-template-list" setup>
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { useModal } from '/@/components/Modal';
import FeTemplateModal from './components/FeTemplateModal.vue';
import { columns, searchFormSchema } from './FeTemplate.data';
import { list, deleteOne, deleteBatch, getExportUrl, getImportUrl } from './FeTemplate.api';
import { Icon } from '/@/components/Icon';

const [registerModal, { openModal }] = useModal();

const { tableContext } = useListPage({
  tableProps: {
    api: list,
    columns: columns,
    canResize: false,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 200,
    },
  },
  exportConfig: {
    url: getExportUrl,
    name: '前端代码模板',
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
  },
});

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

function handleCreate() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

async function batchHandleDelete() {
  await deleteBatch({ ids: selectedRowKeys.value.join(',') }, handleSuccess);
}

function handleSuccess() {
  reload();
}

function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'lowcode:fe_template:edit',
    },
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '删除',
      popConfirm: {
        title: '确定删除吗？',
        confirm: handleDelete.bind(null, record),
      },
      auth: 'lowcode:fe_template:delete',
    },
  ];
}

async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
}

function handleExportXls(name) {
  // useListPage 内置了导出能力
}
</script>

<style scoped></style>
