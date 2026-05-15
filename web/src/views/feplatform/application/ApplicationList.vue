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
            { label: '编辑', onClick: handleEdit.bind(null, record) },
            { label: '绑定Skill', onClick: handleBindSkill.bind(null, record) },
            { label: '删除', onClick: handleDelete.bind(null, record), confirm: true },
          ]"
        />
      </template>
    </BasicTable>

    <BasicModal
      v-if="visible"
      :visible="visible"
      :title="isUpdate ? '编辑应用' : '新增应用'"
      @cancel="handleCancel"
      @ok="handleOk"
    >
      <BasicForm @register="registerForm" />
    </BasicModal>

    <BasicModal
      v-if="skillVisible"
      title="绑定Skill"
      :visible="skillVisible"
      width="800px"
      @cancel="skillVisible = false"
      @ok="handleBindSkillOk"
    >
      <div class="skill-bind-container">
        <a-row :gutter="16">
          <a-col :span="11">
            <div class="skill-section">
              <h4>可选Skill列表</h4>
              <a-tree-select
                v-model:value="selectedSkills"
                :tree-data="availableSkills"
                :multiple="true"
                placeholder="请选择Skill"
                style="width: 100%"
                show-search
                tree-checkable
              />
            </div>
          </a-col>
          <a-col :span="2" class="text-center">
            <a-button type="primary" @click="addSelected" block>></a-button>
            <a-button @click="removeSelected" block><</a-button>
          </a-col>
          <a-col :span="11">
            <div class="skill-section">
              <h4>已绑定Skill</h4>
              <a-list
                :data-source="boundSkills"
                :bordered="true"
                :locale="{ emptyText: '暂无绑定的Skill' }"
              >
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <a-list-item-meta-title>{{ item.name }}</a-list-item-meta-title>
                      <a-list-item-meta-description>{{ item.description }}</a-list-item-meta-description>
                    </a-list-item-meta>
                    <a-button size="small" @click="removeSkill(item.id)">移除</a-button>
                  </a-list-item>
                </template>
              </a-list>
            </div>
          </a-col>
        </a-row>
      </div>
    </BasicModal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { BasicModal, useModal } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { columns, searchFormSchema, formSchema } from './Application.data';
import {
  list,
  saveOrUpdate,
  deleteOne,
  queryById,
  getSkillList,
  bindSkills,
} from '/@/api/feplatform/application';
import { list as skillList } from '/@/api/feplatform/skill';

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
    width: 180,
    title: '操作',
    dataIndex: 'action',
    slots: { customRender: 'action' },
  },
});

const { registerForm, formContext, resetFields } = useForm({
  labelWidth: 120,
  schemas: formSchema,
});

const { emit } = defineEmits<{
  (e: 'refresh'): void;
}>();

const visible = ref(false);
const isUpdate = ref(false);
const currentRecord = ref({});

const skillVisible = ref(false);
const currentAppId = ref('');
const selectedSkills = ref<string[]>([]);
const availableSkills = ref<any[]>([]);
const boundSkills = ref<any[]>([]);

const handleAdd = () => {
  visible.value = true;
  isUpdate.value = false;
  resetFields();
};

const handleEdit = async (record) => {
  visible.value = true;
  isUpdate.value = true;
  currentRecord.value = record;
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
  if (values.techStack && typeof values.techStack === 'string') {
    try {
      values.techStack = JSON.parse(values.techStack);
    } catch (e) {
      console.error('Invalid techStack JSON');
    }
  }
  const res = await saveOrUpdate(values, isUpdate.value);
  if (res.success) {
    visible.value = false;
    tableContext?.reload();
  }
};

const handleBindSkill = async (record) => {
  currentAppId.value = record.id;
  skillVisible.value = true;
  await loadSkills();
};

const loadSkills = async () => {
  const [appSkillsRes, allSkillsRes] = await Promise.all([
    getSkillList(currentAppId.value),
    skillList({ pageNo: 1, pageSize: 100 }),
  ]);
  
  if (appSkillsRes.success) {
    boundSkills.value = appSkillsRes.result;
  }
  
  if (allSkillsRes.success) {
    const boundIds = boundSkills.value.map(s => s.id);
    availableSkills.value = allSkillsRes.result.records
      .filter(s => !boundIds.includes(s.id))
      .map(s => ({
        title: s.name,
        value: s.id,
        key: s.id,
      }));
  }
};

const addSelected = () => {
  selectedSkills.value.forEach(skillId => {
    const skill = availableSkills.value.find(s => s.value === skillId);
    if (skill) {
      boundSkills.value.push({ id: skill.value, name: skill.title });
      availableSkills.value = availableSkills.value.filter(s => s.value !== skillId);
    }
  });
  selectedSkills.value = [];
};

const removeSelected = () => {
  boundSkills.value.forEach(skill => {
    availableSkills.value.push({
      title: skill.name,
      value: skill.id,
      key: skill.id,
    });
  });
  boundSkills.value = [];
};

const removeSkill = (skillId) => {
  const skill = boundSkills.value.find(s => s.id === skillId);
  if (skill) {
    availableSkills.value.push({
      title: skill.name,
      value: skill.id,
      key: skill.id,
    });
    boundSkills.value = boundSkills.value.filter(s => s.id !== skillId);
  }
};

const handleBindSkillOk = async () => {
  const skillIds = boundSkills.value.map(s => s.id);
  const res = await bindSkills(currentAppId.value, skillIds);
  if (res.success) {
    skillVisible.value = false;
    tableContext?.reload();
  }
};

onMounted(() => {});
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.application-table {
  margin-top: 16px;
}

.skill-bind-container {
  padding: 16px;
}

.skill-section {
  height: 300px;
  overflow-y: auto;
}

.skill-section h4 {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
}
</style>