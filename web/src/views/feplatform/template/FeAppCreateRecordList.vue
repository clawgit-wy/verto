<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" preIcon="ant-design:delete-outlined">批量删除</a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '查看', onClick: handleView.bind(null, record) },
            {
              label: '删除',
              popConfirm: {
                title: '是否确认删除',
                confirm: handleDelete.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>

    <BasicDrawer
      title="创建记录详情"
      :width="600"
      :open="viewVisible"
      @close="viewVisible = false"
    >
      <a-descriptions :column="1" bordered v-if="viewData">
        <a-descriptions-item label="应用名称">{{ viewData.appName }}</a-descriptions-item>
        <a-descriptions-item label="应用编码">{{ viewData.appCode }}</a-descriptions-item>
        <a-descriptions-item label="模版ID">{{ viewData.templateId }}</a-descriptions-item>
        <a-descriptions-item label="版本ID">{{ viewData.versionId }}</a-descriptions-item>
        <a-descriptions-item label="输出类型">
          {{ viewData.outputType === 'gitlab' ? '创建GitLab仓库' : '下载ZIP' }}
        </a-descriptions-item>
        <a-descriptions-item label="GitLab地址">{{ viewData.gitlabUrl || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ viewData.status }}</a-descriptions-item>
        <a-descriptions-item label="错误信息">{{ viewData.errorMessage || '-' }}</a-descriptions-item>
        <a-descriptions-item label="参数">
          <pre style="max-height: 200px; overflow: auto;">{{ JSON.stringify(viewData.params, null, 2) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="创建人">{{ viewData.creator }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ viewData.createTime }}</a-descriptions-item>
      </a-descriptions>
    </BasicDrawer>
  </div>
</template>

<script lang="ts" name="feplatform-app-create-record-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { BasicDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { recordColumns, recordSearchSchema } from './Template.data';
  import {
    recordList,
    recordDelete,
    recordBatchDelete,
    recordQueryById,
  } from '/@/api/feplatform/template';

  const { tableContext } = useListPage({
    tableProps: {
      title: '应用创建记录',
      api: recordList,
      columns: recordColumns,
      canResize: true,
      formConfig: {
        schemas: recordSearchSchema,
        autoSubmitOnEnter: true,
      },
      actionColumn: {
        width: 150,
        fixed: 'right',
        title: '操作',
        dataIndex: 'action',
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  const viewVisible = ref(false);
  const viewData = ref<Recordable | null>(null);

  async function handleView(record) {
    const res = await recordQueryById(record.id);
    viewData.value = res || record;
    viewVisible.value = true;
  }

  function handleDelete(record) {
    recordDelete({ id: record.id }, reload);
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    recordBatchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
  }
</script>