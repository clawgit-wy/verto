<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">新增流水线</a-button>
        <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" preIcon="ant-design:delete-outlined">批量删除</a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '触发构建', onClick: handleTrigger.bind(null, record) },
            { label: '同步历史', onClick: handleSync.bind(null, record) },
            { label: '编辑', onClick: handleEdit.bind(null, record) },
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
      :title="isUpdate ? '编辑流水线' : '新增流水线'"
      :width="720"
      :open="editVisible"
      showFooter
      @ok="handleOk"
      @close="editVisible = false"
    >
      <BasicForm @register="registerForm" />
    </BasicDrawer>
  </div>
</template>

<script lang="ts" name="feplatform-pipeline-list" setup>
  import { ref, watch, nextTick } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { BasicDrawer } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { pipelineColumns, pipelineSearchSchema, pipelineFormSchema } from './Cicd.data';
  import {
    pipelineList,
    pipelineSaveOrUpdate,
    pipelineDelete,
    pipelineBatchDelete,
    pipelineQueryById,
    pipelineTriggerBuild,
    pipelineSyncBuilds,
  } from '/@/api/feplatform/cicd';

  const { createMessage } = useMessage();

  const { tableContext } = useListPage({
    tableProps: {
      title: '流水线管理',
      api: pipelineList,
      columns: pipelineColumns,
      canResize: true,
      formConfig: { schemas: pipelineSearchSchema, autoSubmitOnEnter: true },
      actionColumn: { width: 280, fixed: 'right', title: '操作', dataIndex: 'action' },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    schemas: pipelineFormSchema,
    showActionButtonGroup: false,
  });

  const editVisible = ref(false);
  const isUpdate = ref(false);
  const editData = ref<Recordable | null>(null);

  watch(editVisible, async (val) => {
    if (val) {
      await nextTick();
      resetFields();
      if (editData.value) {
        setFieldsValue(editData.value);
        editData.value = null;
      }
    }
  });

  function handleAdd() {
    isUpdate.value = false;
    editVisible.value = true;
  }

  async function handleEdit(record) {
    isUpdate.value = true;
    const res = await pipelineQueryById(record.id);
    editData.value = res || record;
    editVisible.value = true;
  }

  function handleDelete(record) {
    pipelineDelete({ id: record.id }, reload);
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    pipelineBatchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
  }

  async function handleTrigger(record) {
    try {
      await pipelineTriggerBuild(record.id);
      createMessage.success('构建已触发');
    } catch (e: any) {
      createMessage.error('触发失败: ' + (e?.message || e));
    }
  }

  async function handleSync(record) {
    try {
      const res: any = await pipelineSyncBuilds(record.id);
      createMessage.success('同步完成，新增 ' + (res ?? 0) + ' 条记录');
    } catch (e: any) {
      createMessage.error('同步失败: ' + (e?.message || e));
    }
  }

  async function handleOk() {
    const values = await validate();
    if (!values) return;
    await pipelineSaveOrUpdate(values, isUpdate.value);
    createMessage.success(isUpdate.value ? '编辑成功' : '新增成功');
    editVisible.value = false;
    reload();
  }
</script>