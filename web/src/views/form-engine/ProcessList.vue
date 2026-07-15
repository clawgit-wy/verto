<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">新建流程</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="getStatusColor(record.status)">{{ getStatusLabel(record.status) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'processName'">
          <div>
            <div style="font-weight: 500">{{ record.processName }}</div>
            <div style="font-size: 12px; color: #999">{{ record.processCode }}</div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'templateName'">
          <div>
            <div>{{ record.templateName }}</div>
            <a-tag v-if="record.version" size="small" color="blue">{{ record.version }}</a-tag>
          </div>
        </template>
      </template>
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '设计', icon: 'ant-design:highlight-outlined', onClick: handleDesign.bind(null, record) },
            { label: 'Schema', icon: 'ant-design:setting-outlined', onClick: handleSchemaConfig.bind(null, record) },
            { label: 'BPM接口', icon: 'ant-design:api-outlined', onClick: handleBpmApiConfig.bind(null, record) },
            { label: '导入', icon: 'ant-design:import-outlined', onClick: handleImport.bind(null, record) },
            { label: '仿真', icon: 'ant-design:play-circle-outlined', onClick: handleSimulate.bind(null, record) },
            { label: '导出', icon: 'ant-design:download-outlined', onClick: handleExport.bind(null, record) },
          ]"
          :dropDownActions="[
            { label: '编辑', onClick: handleEdit.bind(null, record) },
            {
              label: '删除',
              popConfirm: { title: '确认删除该流程？', confirm: handleDelete.bind(null, record) },
            },
          ]"
        />
      </template>
    </BasicTable>

    <!-- 新建/编辑流程 - 全屏弹窗 -->
    <ProcessFormModal @register="registerFormModal" @success="reload" />

    <!-- 导入流程定义 - 全屏弹窗 -->
    <ProcessImportModal @register="registerImportModal" @success="reload" />

    <!-- 流程表单Schema配置 - 全屏弹窗（流程级，所有节点共享） -->
    <NodeSchemaConfigModal @register="registerSchemaModal" @success="handleSchemaConfigSuccess" />

    <!-- BPM对接接口配置 - 全屏弹窗 -->
    <BpmApiConfigModal @register="registerBpmApiModal" />
  </div>
</template>

<script lang="ts" name="form-engine-process-list" setup>
  // update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】ProcessList 对齐JeecgBoot列表标准（BasicTable+全屏弹窗）---
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRouter } from 'vue-router';
  import { processColumns, processSearchSchema, PROCESS_STATUS } from './FormEngine.data';
  import { getProcessList, deleteProcess, saveSchemaFields, getSchemaFields } from '/@/api/form-engine';
  import ProcessFormModal from './components/ProcessFormModal.vue';
  import ProcessImportModal from './components/ProcessImportModal.vue';
  import NodeSchemaConfigModal from './components/NodeSchemaConfigModal.vue';
  import BpmApiConfigModal from './components/BpmApiConfigModal.vue';
  import type { NodeFormField } from './FormEngine.data';

  const { createMessage } = useMessage();
  const router = useRouter();

  const { tableContext } = useListPage({
    tableProps: {
      title: '表单引擎 - 流程列表',
      api: getProcessList,
      columns: processColumns,
      canResize: true,
      formConfig: {
        schemas: processSearchSchema,
        autoSubmitOnEnter: true,
      },
      actionColumn: {
        width: 400,
        fixed: 'right',
        title: '操作',
        dataIndex: 'action',
      },
      beforeFetch: (params) => {
        // jeecg-boot 列表接口通过字段名查询，确保参数名匹配
        return params;
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const [registerFormModal, { openModal: openFormModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [registerSchemaModal, { openModal: openSchemaModal }] = useModal();
  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置弹窗注册---
  const [registerBpmApiModal, { openModal: openBpmApiModal }] = useModal();
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置弹窗注册---

  function getStatusColor(status: string) {
    return PROCESS_STATUS[status]?.color || 'default';
  }

  function getStatusLabel(status: string) {
    return PROCESS_STATUS[status]?.label || status;
  }

  /** 新建流程 */
  function handleAdd() {
    openFormModal(true, { isUpdate: false });
  }

  /** 编辑流程 */
  function handleEdit(record: Recordable) {
    openFormModal(true, { isUpdate: true, record });
  }

  /** 设计表单 - 跳转设计器页面（SPA导航） */
  function handleDesign(record: Recordable) {
    router.push({
      path: '/form-engine/designer',
      query: { processId: record.id, templateId: record.templateId },
    });
  }

  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】流程级表单Schema配置入口（所有节点共享同一套字段定义）---
  /**
   * 打开流程表单Schema配置弹窗
   * 先从后端加载该流程已保存的 formSchemaFields，再传入弹窗
   */
  async function handleSchemaConfig(record: Recordable) {
    try {
      // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】改用独立表fe_process_schema_field的查询接口---
      const data: any = await getSchemaFields(record.id);
      const existingFields: NodeFormField[] = Array.isArray(data) ? data : (data?.result || []);
      // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】改用独立表fe_process_schema_field的查询接口---
      openSchemaModal(true, {
        process: {
          id: record.id,
          processName: record.processName,
          processCode: record.processCode,
        },
        fields: existingFields,
      });
    } catch (err) {
      // 加载失败时仍打开弹窗（空字段）
      openSchemaModal(true, {
        process: {
          id: record.id,
          processName: record.processName,
          processCode: record.processCode,
        },
        fields: [],
      });
    }
  }

  /**
   * 流程表单Schema配置保存回调
   * 将配置的字段列表持久化到 fe_process_schema_field 表
   */
  async function handleSchemaConfigSuccess(data: { processId: string; fields: NodeFormField[] }) {
    if (!data.processId) {
      createMessage.warning('缺少流程ID，保存失败');
      return;
    }
    try {
      // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】改用独立表fe_process_schema_field的批量保存接口---
      await saveSchemaFields(data.processId, data.fields);
      // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】改用独立表fe_process_schema_field的批量保存接口---
      createMessage.success(`流程表单Schema已保存（共 ${data.fields.length} 个字段）`);
    } catch (err) {
      createMessage.error('Schema保存失败，请重试');
      console.error('[form-engine] save formSchemaFields failed', err);
    }
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】流程级表单Schema配置入口（所有节点共享同一套字段定义）---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置入口---
  /** 打开BPM对接接口配置弹窗 */
  function handleBpmApiConfig(record: Recordable) {
    openBpmApiModal(true, {
      process: {
        id: record.id,
        processName: record.processName,
        processCode: record.processCode,
      },
    });
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置入口---

  /** 导入流程定义 - 打开全屏弹窗 */
  function handleImport(record: Recordable) {
    openImportModal(true, { record });
  }

  /** 仿真测试 - 跳转仿真页面（SPA导航） */
  function handleSimulate(record: Recordable) {
    router.push({
      path: '/form-engine/simulation',
      query: { processId: record.id, templateId: record.templateId },
    });
  }

  /** 导出代码 - 跳转导出页面（SPA导航） */
  function handleExport(record: Recordable) {
    router.push({
      path: '/form-engine/export',
      query: { processId: record.id, templateId: record.templateId },
    });
  }

  /** 删除流程 */
  async function handleDelete(record: Recordable) {
    await deleteProcess(record.id);
    createMessage.success('删除成功');
    reload();
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单引擎】ProcessList 对齐JeecgBoot列表标准（BasicTable+全屏弹窗）---
</script>
