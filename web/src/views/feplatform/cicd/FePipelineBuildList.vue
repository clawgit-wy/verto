<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" preIcon="ant-design:delete-outlined">批量删除</a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '详情', onClick: handleView.bind(null, record) },
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
      title="构建详情"
      :width="640"
      :open="viewVisible"
      @close="viewVisible = false"
    >
      <a-descriptions :column="1" bordered v-if="viewData">
        <a-descriptions-item label="流水线ID">{{ viewData.pipelineId }}</a-descriptions-item>
        <a-descriptions-item label="构建号">{{ viewData.buildNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">{{ viewData.status }}</a-descriptions-item>
        <a-descriptions-item label="耗时(ms)">{{ viewData.duration }}</a-descriptions-item>
        <a-descriptions-item label="触发用户">{{ viewData.triggerUser }}</a-descriptions-item>
        <a-descriptions-item label="提交SHA">{{ viewData.commitSha }}</a-descriptions-item>
        <a-descriptions-item label="技术栈">{{ viewData.techStack }}</a-descriptions-item>
        <a-descriptions-item label="制品版本">{{ viewData.artifactVersion }}</a-descriptions-item>
        <a-descriptions-item label="模板版本">{{ viewData.templateVersion }}</a-descriptions-item>
        <a-descriptions-item label="质量得分">{{ viewData.qualityScore }}</a-descriptions-item>
        <a-descriptions-item label="豁免检查项">
          <pre style="max-height: 150px; overflow: auto;">{{ viewData.checkLevelExemptions || '-' }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="框架信息">
          <pre style="max-height: 150px; overflow: auto;">{{ viewData.frameworkInfo || '-' }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="完成时间">{{ viewData.finishTime }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ viewData.createTime }}</a-descriptions-item>
      </a-descriptions>
    </BasicDrawer>
  </div>
</template>

<script lang="ts" name="feplatform-pipeline-build-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { BasicDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { buildColumns, buildSearchSchema } from './Cicd.data';
  import {
    buildList,
    buildDelete,
    buildBatchDelete,
    buildQueryById,
  } from '/@/api/feplatform/cicd';

  const { tableContext } = useListPage({
    tableProps: {
      title: '构建记录',
      api: buildList,
      columns: buildColumns,
      canResize: true,
      formConfig: { schemas: buildSearchSchema, autoSubmitOnEnter: true },
      actionColumn: { width: 150, fixed: 'right', title: '操作', dataIndex: 'action' },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  const viewVisible = ref(false);
  const viewData = ref<Recordable | null>(null);

  async function handleView(record) {
    const res = await buildQueryById(record.id);
    viewData.value = res || record;
    viewVisible.value = true;
  }

  function handleDelete(record) {
    buildDelete({ id: record.id }, reload);
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    buildBatchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
  }
</script>