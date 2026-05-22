<template>
  <div class="app-container">
    <BasicTable @register="registerTable" class="application-table">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <template #icon><PlusOutlined /></template>
          新增应用
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
      title="应用详情"
      :width="600"
      :open="viewVisible"
      @close="viewVisible = false"
    >
      <Description :column="1" :data="viewData" :schema="descSchema" />
    </BasicDrawer>

    <BasicDrawer
      :title="isUpdate ? '编辑应用' : '新增应用'"
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

<script lang="ts" name="feplatform-application-list" setup>
import { ref, watch, nextTick } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { BasicDrawer } from '/@/components/Drawer';
import { BasicForm, useForm } from '/@/components/Form';
import { Description } from '/@/components/Description';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { columns, searchFormSchema, formSchema, descSchema } from './Application.data';
import {
  list,
  saveOrUpdate,
  deleteOne,
  queryById,
} from '/@/api/feplatform/application';
import { list as developerList } from '/@/api/feplatform/developer';

const { createMessage } = useMessage();

const { tableContext } = useListPage({
  tableProps: {
    title: '应用列表',
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
    await fillDeveloperNames(res);
    viewData.value = res;
    viewVisible.value = true;
  }
};

const fillDeveloperNames = async (data: Recordable) => {
  const ids = [data.creatorId, data.ownerId].filter((id) => !!id);
  if (ids.length === 0) return;
  try {
    const devRes = await developerList({ pageNo: 1, pageSize: 1000 });
    const records = Array.isArray(devRes) ? devRes : devRes?.records || [];
    const map = new Map<string, string>();
    records.forEach((item: any) => map.set(item.id, item.realName));
    if (data.creatorId) {
      data.creatorId_dictText = map.get(data.creatorId) || data.creatorId;
    }
    if (data.ownerId) {
      data.ownerId_dictText = map.get(data.ownerId) || data.ownerId;
    }
  } catch (e) {
    console.error('加载开发者列表失败', e);
  }
};

const handleEdit = async (record) => {
  isUpdate.value = true;
  const res = await queryById(record.id);
  if (res) {
    editData.value = res;
    editVisible.value = true;
  }
};

const handleDelete = (record) => {
  deleteOne({ id: record.id }, reload);
};

const handleOk = async () => {
  const values = await validate();
  if (!values) return;
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

.application-table {
  margin-top: 16px;
}
</style>
