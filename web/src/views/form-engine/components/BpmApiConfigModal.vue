<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="modalTitle"
    :defaultFullscreen="true"
    :showOkBtn="false"
    cancelText="关闭"
  >
    <div class="bpm-api-container">
      <a-alert
        message="每个流程独立拥有7个BPM对接接口（对应docs/流程配置.txt规范）。创建流程时自动生成，可修改接口URL并一键同步到BPM平台。"
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

      <!-- 操作栏 -->
      <div class="api-toolbar">
        <a-space>
          <a-button type="primary" :loading="syncing" @click="handleSync">
            <template #icon><CloudUploadOutlined /></template>
            一键同步到BPM平台
          </a-button>
          <a-button @click="handleGenerate" :loading="generating">
            <template #icon><ReloadOutlined /></template>
            补生成接口记录
          </a-button>
          <a-button @click="loadApiList">
            <template #icon><RefreshOutlined /></template>
            刷新
          </a-button>
        </a-space>
        <a-tag v-if="syncResult" :color="syncResultColor" style="margin-left: 12px">
          {{ syncResult }}
        </a-tag>
      </div>

      <!-- 接口列表表格 -->
      <a-table
        :dataSource="apiList"
        :columns="tableColumns"
        :pagination="false"
        size="small"
        :rowKey="(r) => r.id"
        :loading="loading"
        class="api-table"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'index'">{{ index + 1 }}</template>
          <template v-if="column.dataIndex === 'apiName'">
            <div>
              <span style="font-weight: 600">{{ record.apiName }}</span>
              <a-tag size="small" :color="getApiKeyColor(record.apiKey)" style="margin-left: 4px">
                {{ record.apiKey }}
              </a-tag>
            </div>
          </template>
          <template v-if="column.dataIndex === 'apiMethod'">
            <a-tag :color="record.apiMethod === 'GET' ? 'green' : 'blue'">{{ record.apiMethod }}</a-tag>
          </template>
          <template v-if="column.dataIndex === 'apiUrl'">
            <a-input
              v-model:value="record.apiUrl"
              size="small"
              style="width: 100%"
              @blur="handleUrlChange(record)"
            />
          </template>
          <template v-if="column.dataIndex === 'syncStatus'">
            <a-tag :color="getSyncStatusColor(record.syncStatus)">
              {{ getSyncStatusLabel(record.syncStatus) }}
            </a-tag>
            <div v-if="record.syncTime" style="font-size: 11px; color: #999; margin-top: 2px">
              {{ record.syncTime }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <a-button size="small" type="link" @click="handleCopyUrl(record)">复制URL</a-button>
          </template>
        </template>
      </a-table>

      <!-- 接口说明 -->
      <a-divider />
      <div class="api-descriptions">
        <div class="desc-title">接口规范说明（对应 docs/流程配置.txt）</div>
        <a-timeline>
          <a-timeline-item v-for="item in apiSpecList" :key="item.key" :color="item.color">
            <div>
              <span class="spec-key">{{ item.key }}</span>
              <span class="spec-name">{{ item.name }}</span>
            </div>
            <div class="spec-desc">{{ item.desc }}</div>
          </a-timeline-item>
        </a-timeline>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" name="bpm-api-config-modal" setup>
  // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置弹窗---
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    CloudUploadOutlined,
    ReloadOutlined,
    RestOutlined as RefreshOutlined,
  } from '@ant-design/icons-vue';
  import { getBpmApiList, updateBpmApi, syncBpmApis, generateBpmApis } from '/@/api/form-engine';

  const { createMessage } = useMessage();
  const currentProcess = ref<any>(null);
  const apiList = ref<any[]>([]);
  const loading = ref(false);
  const syncing = ref(false);
  const generating = ref(false);
  const syncResult = ref('');

  const modalTitle = computed(() => {
    const name = currentProcess.value?.processName || '';
    return `BPM对接接口配置${name ? ' - ' + name : ''}`;
  });

  const syncResultColor = computed(() => {
    if (!syncResult.value) return 'default';
    return syncResult.value.includes('失败') ? 'red' : 'green';
  });

  const tableColumns = [
    { title: '#', dataIndex: 'index', width: 40, align: 'center' },
    { title: '接口名称', dataIndex: 'apiName', width: 220 },
    { title: '方法', dataIndex: 'apiMethod', width: 70, align: 'center' },
    { title: '接口URL', dataIndex: 'apiUrl', width: 350 },
    { title: '同步状态', dataIndex: 'syncStatus', width: 130 },
    { title: '操作', dataIndex: 'action', width: 80, align: 'center' },
  ];

  /** 接口规范说明 */
  const apiSpecList = [
    { key: 'formFields', name: '获取业务表单字段接口', desc: '获取异构系统表单模板及字段定义', color: 'blue' },
    { key: 'roles', name: '业务系统角色配置接口', desc: '获取业务角色，用于流程节点处理人配置', color: 'green' },
    { key: 'eventListener', name: '流程事件调用业务监听', desc: '流程运行过程中调用业务监听器', color: 'orange' },
    { key: 'formValues', name: '获取业务表单字段值接口', desc: '流程运行中获取业务系统表单字段值', color: 'cyan' },
    { key: 'templateEvent', name: '流程模板事件接口', desc: '流程模板保存/更新/删除事件通知', color: 'purple' },
    { key: 'callback', name: '流程事件业务监听接口', desc: '流程事件触发的业务监听，可能触发流程变更', color: 'magenta' },
    { key: 'templates', name: '获取业务表单模板接口', desc: '获取业务系统表单模板，用于绑定表单', color: 'gold' },
  ];

  function getApiKeyColor(key: string): string {
    const found = apiSpecList.find((s) => s.key === key);
    return found?.color || 'default';
  }

  function getSyncStatusColor(status: string): string {
    const map: Record<string, string> = {
      unsynced: 'default',
      synced: 'green',
      failed: 'red',
    };
    return map[status] || 'default';
  }

  function getSyncStatusLabel(status: string): string {
    const map: Record<string, string> = {
      unsynced: '未同步',
      synced: '已同步',
      failed: '同步失败',
    };
    return map[status] || status;
  }

  /** 加载接口列表 */
  async function loadApiList() {
    const processId = currentProcess.value?.id || currentProcess.value?.processId;
    if (!processId) return;
    loading.value = true;
    try {
      const data: any = await getBpmApiList(processId);
      apiList.value = Array.isArray(data) ? data : (data?.result || []);
    } catch (err) {
      console.error('[bpm-api] loadApiList failed', err);
      createMessage.error('加载接口列表失败');
    } finally {
      loading.value = false;
    }
  }

  /** URL修改后自动保存 */
  async function handleUrlChange(record: any) {
    try {
      await updateBpmApi({ id: record.id, apiUrl: record.apiUrl });
      createMessage.success(`${record.apiName} URL已更新`);
    } catch (err) {
      createMessage.error('更新失败');
    }
  }

  /** 一键同步到BPM平台 */
  async function handleSync() {
    const processId = currentProcess.value?.id || currentProcess.value?.processId;
    if (!processId) return;
    syncing.value = true;
    syncResult.value = '';
    try {
      const data: any = await syncBpmApis(processId);
      const result = data?.result || data || {};
      syncResult.value = result.syncResult || '同步完成';
      createMessage.success('同步完成');
      await loadApiList();
    } catch (err) {
      syncResult.value = '同步失败';
      createMessage.error('同步失败');
    } finally {
      syncing.value = false;
    }
  }

  /** 补生成接口记录 */
  async function handleGenerate() {
    const processId = currentProcess.value?.id || currentProcess.value?.processId;
    if (!processId) return;
    generating.value = true;
    try {
      await generateBpmApis(processId);
      createMessage.success('接口记录生成成功');
      await loadApiList();
    } catch (err) {
      createMessage.error('生成失败');
    } finally {
      generating.value = false;
    }
  }

  /** 复制URL到剪贴板 */
  async function handleCopyUrl(record: any) {
    try {
      await navigator.clipboard.writeText(record.apiUrl || '');
      createMessage.success('URL已复制');
    } catch {
      createMessage.warning('复制失败，请手动复制');
    }
  }

  const [registerModal] = useModalInner((data) => {
    currentProcess.value = data?.process || null;
    loadApiList();
  });
  // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】BPM对接接口配置弹窗---
</script>

<style lang="less" scoped>
  .bpm-api-container {
    padding: 0 4px;
  }

  .api-toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }

  .api-table {
    :deep(.ant-table-cell) {
      padding: 6px 8px !important;
    }
  }

  .api-descriptions {
    margin-top: 8px;

    .desc-title {
      font-weight: 600;
      font-size: 14px;
      margin-bottom: 12px;
    }

    .spec-key {
      font-family: monospace;
      font-size: 12px;
      color: #1890ff;
      margin-right: 8px;
    }

    .spec-name {
      font-weight: 600;
    }

    .spec-desc {
      font-size: 12px;
      color: #666;
      margin-top: 2px;
    }
  }
</style>
