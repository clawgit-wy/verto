<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">新增版本</a-button>
        <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" preIcon="ant-design:delete-outlined">批量删除</a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
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
      :title="isUpdate ? '编辑模版版本' : '新增模版版本'"
      :width="640"
      :open="editVisible"
      showFooter
      @ok="handleOk"
      @close="editVisible = false"
    >
      <BasicForm @register="registerForm" />
    </BasicDrawer>
  </div>
</template>

<script lang="ts" name="feplatform-template-version-list" setup>
  import { ref, watch, nextTick } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { BasicDrawer } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { versionColumns, versionSearchSchema, versionFormSchema } from './Template.data';
  import {
    versionList,
    versionSaveOrUpdate,
    versionDelete,
    versionBatchDelete,
    versionQueryById,
  } from '/@/api/feplatform/template';

  const { createMessage } = useMessage();

  const { tableContext } = useListPage({
    tableProps: {
      title: '模版版本',
      api: versionList,
      columns: versionColumns,
      canResize: true,
      formConfig: {
        schemas: versionSearchSchema,
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

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    schemas: versionFormSchema,
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
    const res = await versionQueryById(record.id);
    editData.value = res || record;
    editVisible.value = true;
  }

  function handleDelete(record) {
    versionDelete({ id: record.id }, reload);
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    versionBatchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
  }

  async function handleOk() {
    const values = await validate();
    if (!values) return;
    await versionSaveOrUpdate(values, isUpdate.value);
    createMessage.success(isUpdate.value ? '编辑成功' : '新增成功');
    editVisible.value = false;
    reload();
  }
</script>