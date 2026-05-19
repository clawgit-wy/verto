<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    :width="800"
    @ok="handleSubmit"
    :okButtonProps="{ disabled: !showFooter }"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" name="fe-template-modal" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../FeTemplate.data';
import { add, edit, queryById } from '../FeTemplate.api';

const emit = defineEmits(['success', 'register']);
const isUpdate = ref(true);
const showFooter = ref(true);

const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 },
});

const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  resetFields();
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  showFooter.value = !!data?.showFooter;

  if (unref(isUpdate)) {
    let record = data.record;
    if (record.id) {
      const res = await queryById(record.id);
      if (res.success) {
        record = res.result;
      }
    }
    setFieldsValue({
      ...record,
    });
  }
});

const getTitle = computed(() => (!unref(isUpdate) ? '新增模板' : unref(showFooter) ? '编辑模板' : '模板详情'));

async function handleSubmit() {
  try {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    if (unref(isUpdate)) {
      await edit(values);
    } else {
      await add(values);
    }
    closeModal();
    emit('success');
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
