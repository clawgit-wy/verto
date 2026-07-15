<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" name="process-form-modal" setup>
  // update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】新建/编辑流程全屏弹窗---
  import { computed, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { processFormSchema } from '../FormEngine.data';
  import { addProcess, editProcess, getTemplateFormList } from '/@/api/form-engine';

  const emit = defineEmits(['success']);
  const { createMessage } = useMessage();
  const isUpdate = ref(true);

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: processFormSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate) && data?.record) {
      // 加载模板名称
      let record = { ...data.record };
      if (!record.templateName && record.templateId) {
        try {
          const templates = await getTemplateFormList();
          const tmpl = templates?.find((t) => t.id === record.templateId);
          if (tmpl) record.templateName = tmpl.templateName;
        } catch (e) {
          // ignore
        }
      }
      setFieldsValue(record);
    }
  });

  const getTitle = computed(() => (!unref(isUpdate) ? '新建流程' : '编辑流程'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      if (unref(isUpdate)) {
        await editProcess(values);
      } else {
        await addProcess(values);
      }
      createMessage.success(unref(isUpdate) ? '编辑成功' : '新建成功');
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单引擎】新建/编辑流程全屏弹窗---
</script>
