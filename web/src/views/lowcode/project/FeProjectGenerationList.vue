<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:download-outlined" @click="handleExportXls('前端项目生成记录')"> 导出</a-button>
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
  </div>
</template>

<script lang="ts" name="fe-project-generation-list" setup>
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { columns, searchFormSchema } from './FeProjectGeneration.data';
import { list, deleteOne, deleteBatch, getExportUrl } from './FeProjectGeneration.api';
import { Icon } from '/@/components/Icon';

const { tableContext } = useListPage({
  tableProps: {
    api: list,
    columns: columns,
    canResize: false,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
  },
  exportConfig: {
    url: getExportUrl,
    name: '前端项目生成记录',
  },
});

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

async function batchHandleDelete() {
  await deleteBatch({ ids: selectedRowKeys.value.join(',') }, handleSuccess);
}

function handleSuccess() {
  reload();
}

function getTableAction(record) {
  return [
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
      auth: 'lowcode:fe_project_gen:delete',
    },
  ];
}

function handleDetail(record: Recordable) {
  // TODO: 打开详情弹窗
}

async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
}

function handleExportXls(name) {
  // useListPage 内置了导出能力
}
</script>

<style scoped></style>
