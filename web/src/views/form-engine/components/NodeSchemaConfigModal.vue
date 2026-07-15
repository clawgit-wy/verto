<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="modalTitle"
    :defaultFullscreen="true"
    okText="保存"
    @ok="handleSubmit"
  >
    <div class="schema-config-container">
      <a-alert
        message="为当前流程配置表单字段Schema（流程级，所有节点共享同一套字段定义）。配置后的字段将在表单设计器中可供关联绑定，并在节点权限配置中按节点设置可见性/只读/可编辑。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <!-- 流程信息 -->
      <a-card size="small" style="margin-bottom: 16px">
        <a-descriptions :column="3" size="small">
          <a-descriptions-item label="流程名称">{{ currentProcess?.processName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="流程编码">{{ currentProcess?.processCode || '-' }}</a-descriptions-item>
          <a-descriptions-item label="流程ID">{{ currentProcess?.id || currentProcess?.processId || '-' }}</a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 字段配置表格 -->
      <div class="field-list-header">
        <span class="section-title">
          表单字段配置
          <a-badge :count="fieldList.length" :offset="[8, -2]" />
        </span>
        <a-space>
          <a-button size="small" @click="handleLoadFromXml" v-if="hasProcessDef">
            <template #icon><ImportOutlined /></template>
            从流程节点提取字段
          </a-button>
          <a-button type="primary" size="small" @click="handleAddField">
            <template #icon><PlusOutlined /></template>
            添加字段
          </a-button>
        </a-space>
      </div>

      <a-table
        :dataSource="fieldList"
        :columns="tableColumns"
        :pagination="false"
        size="small"
        :rowKey="(r) => r._uid"
        class="field-table"
      >
        <template #bodyCell="{ column, record, index }">
          <!-- 序号 -->
          <template v-if="column.dataIndex === 'index'">{{ index + 1 }}</template>

          <!-- 字段标识 -->
          <template v-if="column.dataIndex === 'fieldKey'">
            <a-input
              v-model:value="record.fieldKey"
              placeholder="如 applyReason"
              size="small"
              style="width: 140px"
            />
          </template>

          <!-- 显示名称 -->
          <template v-if="column.dataIndex === 'fieldLabel'">
            <a-input
              v-model:value="record.fieldLabel"
              placeholder="如 申请原因"
              size="small"
              style="width: 120px"
            />
          </template>

          <!-- 字段类型 -->
          <template v-if="column.dataIndex === 'fieldType'">
            <a-select
              v-model:value="record.fieldType"
              size="small"
              style="width: 120px"
              placeholder="选择类型"
            >
              <a-select-option v-for="ft in FIELD_TYPES" :key="ft.value" :value="ft.value">
                {{ ft.label }}
              </a-select-option>
            </a-select>
          </template>

          <!-- 默认值 -->
          <template v-if="column.dataIndex === 'defaultValue'">
            <a-input
              v-model:value="record.defaultValue"
              placeholder="默认值"
              size="small"
              style="width: 120px"
            />
          </template>

          <!-- 验证规则 -->
          <template v-if="column.dataIndex === 'validation'">
            <div class="validation-cell">
              <a-checkbox v-model:checked="record.required" size="small">必填</a-checkbox>
              <a-input
                v-if="record.fieldType === 'string' || record.fieldType === 'stringArray'"
                v-model:value="record.pattern"
                placeholder="正则表达式（可选）"
                size="small"
                style="width: 100%; margin-top: 4px"
              />
              <a-row :gutter="4" style="margin-top: 4px">
                <a-col :span="12">
                  <a-input-number
                    v-model:value="record.min"
                    :placeholder="record.fieldType === 'number' ? '最小值' : '最小长度'"
                    size="small"
                    style="width: 100%"
                  />
                </a-col>
                <a-col :span="12">
                  <a-input-number
                    v-model:value="record.max"
                    :placeholder="record.fieldType === 'number' ? '最大值' : '最大长度'"
                    size="small"
                    style="width: 100%"
                  />
                </a-col>
              </a-row>
            </div>
          </template>

          <!-- 说明 -->
          <template v-if="column.dataIndex === 'description'">
            <a-input
              v-model:value="record.description"
              placeholder="字段说明"
              size="small"
              style="width: 120px"
            />
          </template>

          <!-- 操作 -->
          <template v-if="column.dataIndex === 'action'">
            <a-button
              type="text"
              danger
              size="small"
              @click="handleDeleteField(index)"
            >
              <DeleteOutlined />
            </a-button>
          </template>
        </template>
      </a-table>

      <!-- 空状态提示 -->
      <a-empty
        v-if="fieldList.length === 0"
        description="暂无字段配置，点击「添加字段」开始配置"
        style="margin-top: 24px"
      />

      <!-- 预览JSON -->
      <a-divider v-if="fieldList.length > 0" />
      <div v-if="fieldList.length > 0" class="json-preview-section">
        <div class="section-title">Schema JSON 预览</div>
        <pre class="json-preview">{{ JSON.stringify(previewSchema, null, 2) }}</pre>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" name="node-schema-config-modal" setup>
  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】Schema配置从节点级改为流程级（所有节点共享同一套字段定义）---
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PlusOutlined, DeleteOutlined, ImportOutlined } from '@ant-design/icons-vue';
  import { FIELD_TYPES, type NodeFormField } from '../FormEngine.data';

  const emit = defineEmits(['success']);
  const { createMessage } = useMessage();

  /** 带临时UID的字段（用于表格rowKey） */
  interface TableField extends NodeFormField {
    _uid: string;
  }

  /** 流程信息 */
  interface ProcessInfo {
    id?: string;
    processId?: string;
    processName?: string;
    processCode?: string;
    /** 流程定义XML（可选，用于从节点提取字段） */
    processDef?: string;
  }

  const currentProcess = ref<ProcessInfo | null>(null);
  const fieldList = ref<TableField[]>([]);
  let uidSeq = 0;

  /** 是否有流程定义XML（用于判断是否显示"从流程节点提取字段"按钮） */
  const hasProcessDef = computed(() => {
    const def = currentProcess.value?.processDef;
    return !!(def && typeof def === 'string' && def.trim());
  });

  const modalTitle = computed(() => {
    const name = currentProcess.value?.processName || '';
    return `流程表单Schema配置${name ? ' - ' + name : ''}`;
  });

  const tableColumns = [
    { title: '#', dataIndex: 'index', width: 40, align: 'center' },
    { title: '字段标识', dataIndex: 'fieldKey', width: 150 },
    { title: '显示名称', dataIndex: 'fieldLabel', width: 130 },
    { title: '字段类型', dataIndex: 'fieldType', width: 130 },
    { title: '默认值', dataIndex: 'defaultValue', width: 130 },
    { title: '验证规则', dataIndex: 'validation', width: 220 },
    { title: '说明', dataIndex: 'description', width: 130 },
    { title: '操作', dataIndex: 'action', width: 60, align: 'center' },
  ];

  /** 预览JSON（去除_uid） */
  const previewSchema = computed(() => {
    return fieldList.value.map((f) => {
      const { _uid, ...rest } = f;
      return rest;
    });
  });

  function nextUid() {
    uidSeq += 1;
    return `nf_${uidSeq}`;
  }

  /** 添加字段 */
  function handleAddField() {
    fieldList.value.push({
      _uid: nextUid(),
      fieldKey: '',
      fieldLabel: '',
      fieldType: 'string',
      defaultValue: '',
      required: false,
      pattern: '',
      min: undefined,
      max: undefined,
      validationMessage: '',
      description: '',
    });
  }

  /** 删除字段 */
  function handleDeleteField(index: number) {
    fieldList.value.splice(index, 1);
  }

  /**
   * 从流程定义XML中提取节点字段（基于节点上的 formField 或 field 属性）
   * 这是一个辅助功能：如果XML节点上带有字段信息，可以快速导入
   */
  function handleLoadFromXml() {
    const xml = currentProcess.value?.processDef;
    if (!xml) {
      createMessage.warning('未找到流程定义XML');
      return;
    }
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(xml, 'text/xml');
      const parseErr = doc.querySelector('parsererror');
      if (parseErr) {
        throw new Error('XML 格式错误');
      }
      // 从XML的 field 元素或节点的 field 属性提取
      const fieldElements = doc.querySelectorAll('field, formField, formFields > field');
      if (fieldElements.length === 0) {
        createMessage.info('未在流程定义XML中找到字段元素（field/formField），请手动添加');
        return;
      }
      let added = 0;
      fieldElements.forEach((el) => {
        const fieldKey = el.getAttribute('key') || el.getAttribute('name') || el.getAttribute('id') || '';
        const fieldLabel = el.getAttribute('label') || el.getAttribute('title') || fieldKey;
        const fieldType = el.getAttribute('type') || 'string';
        if (fieldKey && !fieldList.value.some((f) => f.fieldKey === fieldKey)) {
          fieldList.value.push({
            _uid: nextUid(),
            fieldKey,
            fieldLabel,
            fieldType,
            defaultValue: '',
            required: false,
            pattern: '',
            min: undefined,
            max: undefined,
            validationMessage: '',
            description: '',
          });
          added += 1;
        }
      });
      if (added > 0) {
        createMessage.success(`从流程定义中提取了 ${added} 个字段`);
      } else {
        createMessage.info('未提取到新字段（可能已存在）');
      }
    } catch (e) {
      createMessage.error('解析XML失败：' + (e instanceof Error ? e.message : String(e)));
    }
  }

  const [registerModal, { setModalProps, closeModal }] = useModalInner((data) => {
    setModalProps({ confirmLoading: false });
    currentProcess.value = data?.process || null;
    // 加载已有字段配置（流程级，所有节点共享）
    const existingFields = data?.fields || [];
    fieldList.value = existingFields.map((f: NodeFormField) => ({
      ...f,
      _uid: nextUid(),
    }));
  });

  /** 提交保存 */
  function handleSubmit() {
    // 校验字段标识不能重复且不能为空
    const keys = fieldList.value.map((f) => f.fieldKey);
    const emptyKeys = keys.filter((k) => !k.trim());
    if (emptyKeys.length > 0) {
      createMessage.warning('存在字段标识为空的配置，请填写');
      return;
    }
    const duplicates = keys.filter((k, i) => keys.indexOf(k) !== i);
    if (duplicates.length > 0) {
      createMessage.warning(`字段标识重复：${[...new Set(duplicates)].join(', ')}`);
      return;
    }

    // 导出为纯净的 NodeFormField[]
    const cleanFields: NodeFormField[] = fieldList.value.map((f) => {
      const { _uid, ...rest } = f;
      return rest;
    });

    const processId = currentProcess.value?.id || currentProcess.value?.processId;
    emit('success', {
      processId,
      fields: cleanFields,
    });
    closeModal();
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】Schema配置从节点级改为流程级（所有节点共享同一套字段定义）---
</script>

<style lang="less" scoped>
  .schema-config-container {
    padding: 0 4px;
  }

  .field-list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
  }

  .section-title {
    font-weight: 600;
    font-size: 14px;
  }

  .field-table {
    :deep(.ant-table-cell) {
      padding: 6px 4px !important;
    }
  }

  .validation-cell {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .json-preview-section {
    margin-top: 8px;
  }

  .json-preview {
    background: #1e1e2e;
    color: #e5e7eb;
    padding: 12px;
    border-radius: 6px;
    font-family: 'SF Mono', Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    overflow: auto;
    max-height: 300px;
    margin: 0;
  }
</style>
