<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="导出MCP配置" width="70%" :showOkBtn="false">
    <div class="mb-4">
      <a-alert message="选择需要导出的Skill，生成 mcp-config.json 配置文件" type="info" show-icon />
    </div>
    <a-table
      :columns="exportColumns"
      :data-source="skillList"
      :row-selection="rowSelection"
      row-key="code"
      size="small"
      :pagination="false"
    />
    <div class="mt-4" v-if="selectedSkillCodes.length > 0">
      <a-card title="生成的配置" size="small">
        <pre class="config-preview">{{ configJson }}</pre>
        <div class="mt-2 text-right">
          <a-button type="primary" @click="copyConfig">
            <template #icon><CopyOutlined /></template>
            复制到剪贴板
          </a-button>
          <a-button class="ml-2" @click="downloadConfig">
            <template #icon><DownloadOutlined /></template>
            下载文件
          </a-button>
        </div>
      </a-card>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { CopyOutlined, DownloadOutlined } from '@ant-design/icons-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { list, exportMcpConfig } from '/@/api/feplatform/skill';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  const exportColumns = [
    { title: 'Skill名称', dataIndex: 'name', width: 150 },
    { title: '编码', dataIndex: 'code', width: 150 },
    { title: '分类', dataIndex: 'category', customRender: ({ text }) => {
      const map = { official: '官方', business: '业务', app: '应用级' };
      return map[text] || text;
    }, width: 100 },
    { title: '描述', dataIndex: 'description', ellipsis: true },
  ];

  const skillList = ref<any[]>([]);
  const selectedRowKeys = ref<string[]>([]);

  const selectedSkillCodes = computed(() => selectedRowKeys.value);

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: string[]) => {
      selectedRowKeys.value = keys;
    },
  };

  const configJson = ref('');

  const [registerModal] = useModalInner(async () => {
    const res = await list({ pageNo: 1, pageSize: 200, status: 'enable' });
    skillList.value = res.records || [];
    selectedRowKeys.value = [];
    configJson.value = '';
  });

  async function generateConfig() {
    if (selectedSkillCodes.value.length === 0) return;
    const codes = selectedSkillCodes.value.join(',');
    const res = await exportMcpConfig(codes);
    configJson.value = JSON.stringify(res, null, 2);
  }

  async function copyConfig() {
    if (!configJson.value) await generateConfig();
    try {
      await navigator.clipboard.writeText(configJson.value);
      createMessage.success('已复制到剪贴板');
    } catch {
      createMessage.error('复制失败，请手动复制');
    }
  }

  function downloadConfig() {
    if (!configJson.value) return;
    const blob = new Blob([configJson.value], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'mcp-config.json';
    a.click();
    URL.revokeObjectURL(url);
  }
</script>

<style lang="less" scoped>
  .config-preview {
    max-height: 300px;
    overflow: auto;
    padding: 12px;
    background: #1e1e1e;
    color: #d4d4d4;
    border-radius: 4px;
    font-family: 'Fira Code', Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    word-break: break-all;
  }
</style>
