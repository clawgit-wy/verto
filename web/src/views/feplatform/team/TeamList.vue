<template>
  <div class="app-container">
    <BasicTable @register="registerTable" class="team-table">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增团队
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '编辑', onClick: handleEdit.bind(null, record) },
            { label: '删除', onClick: handleDelete.bind(null, record), confirm: true },
          ]"
        />
      </template>
    </BasicTable>

    <BasicModal
      v-if="visible"
      :visible="visible"
      :title="isUpdate ? '编辑团队' : '新增团队'"
      @cancel="handleCancel"
      @ok="handleOk"
    >
      <BasicForm @register="registerForm" />
    </BasicModal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { BasicModal } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { columns, searchFormSchema, formSchema } from './Team.data';
import {
  list,
  saveOrUpdate,
  deleteOne,
  queryById,
} from '/@/api/feplatform/team';

const { registerTable, tableContext } = useTable({
  title: '',
  api: list,
  columns,
  formConfig: {
    labelWidth: 120,
    schemas: searchFormSchema,
  },
  canResize: true,
  showIndexColumn: true,
  actionColumn: {
    width: 120,
    title: '操作',
    dataIndex: 'action',
    slots: { customRender: 'action' },
  },
});

const { registerForm, formContext, resetFields } = useForm({
  labelWidth: 120,
  schemas: formSchema,
});

const visible = ref(false);
const isUpdate = ref(false);

const handleAdd = () => {
  visible.value = true;
  isUpdate.value = false;
  resetFields();
};

const handleEdit = async (record) => {
  visible.value = true;
  isUpdate.value = true;
  resetFields();
  const res = await queryById(record.id);
  if (res.success) {
    formContext?.setFieldsValue(res.result);
  }
};

const handleDelete = (record) => {
  deleteOne({ id: record.id }, () => {
    tableContext?.reload();
  });
};

const handleCancel = () => {
  visible.value = false;
  resetFields();
};

const handleOk = async () => {
  const values = await formContext?.validate();
  if (!values) return;
  const res = await saveOrUpdate(values, isUpdate.value);
  if (res.success) {
    visible.value = false;
    tableContext?.reload();
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.team-table {
  margin-top: 16px;
}
</style>