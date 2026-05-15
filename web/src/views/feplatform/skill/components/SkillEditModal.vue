<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    width="80%"
    :okText="isUpdate ? '保存' : '创建'"
    @ok="handleSubmit"
  >
    <a-tabs v-model:activeKey="activeTab">
      <a-tab-pane key="basic" tab="基本信息">
        <BasicForm @register="registerForm" />
      </a-tab-pane>
      <a-tab-pane key="prompt" tab="Prompt模板" force-render>
        <div class="p-4">
          <a-textarea
            v-model:value="promptContent"
            :rows="20"
            placeholder="请输入Prompt模板内容..."
            class="prompt-editor"
          />
        </div>
      </a-tab-pane>
      <a-tab-pane key="test" tab="测试" v-if="isUpdate">
        <SkillTestPanel :skill-id="skillId" :prompt-template="promptContent" />
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from '../Skill.data';
  import { saveOrUpdate, queryById } from '/@/api/feplatform/skill';
  import SkillTestPanel from './SkillTestPanel.vue';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(false);
  const activeTab = ref('basic');
  const skillId = ref('');
  const promptContent = ref('');

  const [registerForm, { setFieldsValue, getFieldsValue, resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data.isUpdate;
    activeTab.value = data.activeTab || 'basic';

    if (data.isUpdate && data.record) {
      const record = data.record.id ? data.record : await queryById(data.record.id);
      setFieldsValue(record);
      promptContent.value = record.promptTemplate || '';
      skillId.value = record.id;
    } else {
      promptContent.value = '';
      skillId.value = '';
    }
  });

  const getTitle = computed(() => (isUpdate.value ? '编辑Skill' : '新增Skill'));

  async function handleSubmit() {
    try {
      setModalProps({ confirmLoading: true });
      const values = await validate();
      values.promptTemplate = promptContent.value;
      await saveOrUpdate(values, isUpdate.value);
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .prompt-editor {
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
  }
</style>
