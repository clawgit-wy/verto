<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">新增Jenkins实例</a-button>
        <a-button @click="handleBatchDelete" :disabled="!selectedRowKeys.length" preIcon="ant-design:delete-outlined">批量删除</a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '测试连通', onClick: handleTest.bind(null, record) },
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
      :title="isUpdate ? '编辑Jenkins实例' : '新增Jenkins实例'"
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

<script lang="ts" name="feplatform-jenkins-instance-list" setup>
  import { ref, watch, nextTick } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { BasicDrawer } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { jenkinsColumns, jenkinsSearchSchema, jenkinsFormSchema } from './Cicd.data';
  import {
    jenkinsList,
    jenkinsSaveOrUpdate,
    jenkinsDelete,
    jenkinsBatchDelete,
    jenkinsQueryById,
    jenkinsTestConnection,
  } from '/@/api/feplatform/cicd';

  const { createMessage } = useMessage();

  const { tableContext } = useListPage({
    tableProps: {
      title: 'Jenkins实例',
      api: jenkinsList,
      columns: jenkinsColumns,
      canResize: true,
      formConfig: { schemas: jenkinsSearchSchema, autoSubmitOnEnter: true },
      actionColumn: { width: 150, fixed: 'right', title: '操作', dataIndex: 'action' },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 120,
    schemas: jenkinsFormSchema,
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
    const res = await jenkinsQueryById(record.id);
    editData.value = res || record;
    editVisible.value = true;
  }

  function handleDelete(record) {
    jenkinsDelete({ id: record.id }, reload);
  }

  function handleBatchDelete() {
    if (selectedRowKeys.value.length === 0) return;
    jenkinsBatchDelete({ ids: selectedRowKeys.value.join(',') }, reload);
  }

  async function handleTest(record) {
    try {
      const res: any = await jenkinsTestConnection(record.id);
      if (res && res.success) {
        createMessage.success('Jenkins 连通正常');
      } else {
        createMessage.error('连通失败: ' + (res?.error || res?.status || '未知'));
      }
    } catch (e: any) {
      createMessage.error('请求失败: ' + e?.message);
    }
  }

  async function handleOk() {
    const values = await validate();
    if (!values) return;
    await jenkinsSaveOrUpdate(values, isUpdate.value);
    createMessage.success(isUpdate.value ? '编辑成功' : '新增成功');
    editVisible.value = false;
    reload();
  }
</script>