<template>
  <div class="app-container">
    <BasicTable @register="registerTable" class="developer-table">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增人员
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '查看', onClick: handleView.bind(null, record) },
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
      title="人员详情"
      :width="600"
      :open="viewVisible"
      @close="viewVisible = false"
    >
      <Description :column="1" :data="viewData" :schema="descSchema" />
    </BasicDrawer>

    <BasicDrawer
      :title="isUpdate ? '编辑人员' : '新增人员'"
      :width="600"
      :open="editVisible"
      showFooter
      @ok="handleOk"
      @close="editVisible = false"
    >
      <BasicForm @register="registerForm" />
    </BasicDrawer>
  </div>
</template>

<script lang="ts" name="feplatform-developer-list" setup>
import { ref, watch, nextTick } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { BasicDrawer } from '/@/components/Drawer';
import { BasicForm, useForm } from '/@/components/Form';
import { Description } from '/@/components/Description';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { columns, searchFormSchema, formSchema, descSchema } from './Developer.data';
import {
  list,
  saveOrUpdate,
  deleteOne,
  queryById,
} from '/@/api/feplatform/developer';

const { createMessage } = useMessage();

const { tableContext } = useListPage({
  tableProps: {
    title: '人员列表',
    api: list,
    columns,
    canResize: true,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  },
});

const [registerTable, { reload }] = tableContext;

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 120,
  schemas: formSchema,
  showActionButtonGroup: false,
});

const viewVisible = ref(false);
const viewData = ref<Recordable>({});

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

const handleAdd = () => {
  isUpdate.value = false;
  editVisible.value = true;
};

const handleView = async (record) => {
  const res = await queryById(record.id);
  if (res) {
    viewData.value = res;
    viewVisible.value = true;
  }
};

const handleEdit = async (record) => {
  isUpdate.value = true;
  const res = await queryById(record.id);
  if (res) {
    const data = res;
    if (data.skillTags && typeof data.skillTags === 'string') {
      try {
        data.skillTags = JSON.parse(data.skillTags);
      } catch (e) {
        data.skillTags = data.skillTags.split(',').map((s: string) => s.trim()).filter((s: string) => s);
      }
    }
    editData.value = data;
    editVisible.value = true;
  }
};

const handleDelete = (record) => {
  deleteOne({ id: record.id }, reload);
};

const handleOk = async () => {
  const values = await validate();
  if (!values) return;
  if (values.skillTags && typeof values.skillTags === 'string') {
    values.skillTags = values.skillTags.split(',').map((s: string) => s.trim()).filter((s: string) => s);
  }
  await saveOrUpdate(values, isUpdate.value);
  createMessage.success(isUpdate.value ? '编辑成功' : '新增成功');
  editVisible.value = false;
  reload();
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.developer-table {
  margin-top: 16px;
}
</style>
