<template>
  <div class="sandbox-preview">
    <div class="sandbox-header">
      <a-space>
        <a-button type="primary" @click="handleCompile">
          <template #icon><PlayCircleOutlined /></template>
          编译预览
        </a-button>
        <a-button @click="handleReset">
          <template #icon><RefreshOutlined /></template>
          重置
        </a-button>
        <a-button @click="handleExport">
          <template #icon><ExportOutlined /></template>
          导出代码
        </a-button>
      </a-space>
      <a-space>
        <a-tag color="blue">{{ compileStatus }}</a-tag>
        <span v-if="compileTime">编译耗时: {{ compileTime }}ms</span>
      </a-space>
    </div>

    <a-row :gutter="16" class="sandbox-body">
      <a-col :span="12" class="editor-panel">
        <a-card title="SFC代码编辑" :bordered="false">
          <a-textarea
            v-model:value="sfcCode"
            :rows="25"
            class="sfc-editor"
            placeholder="请输入Vue SFC代码..."
          />
        </a-card>
      </a-col>
      <a-col :span="12" class="preview-panel">
        <a-card title="实时预览" :bordered="false">
          <div class="preview-container">
            <div v-if="!compiled" class="empty-state">
              <a-empty description="点击编译预览查看效果" />
            </div>
            <div v-else-if="compileError" class="error-state">
              <a-alert
                message="编译错误"
                description="编译失败: {{ compileError }}"
                type="error"
                show-icon
              />
            </div>
            <iframe
              v-else
              ref="previewIframe"
              class="preview-iframe"
              sandbox="allow-scripts allow-modals"
            />
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :span="24">
        <a-card title="导出配置" :bordered="false">
          <a-tabs type="card">
            <a-tab-pane key="template" tab="SFC模板">
              <a-textarea :value="sfcCode" :rows="8" readonly class="export-textarea" />
            </a-tab-pane>
            <a-tab-pane key="json" tab="JSON配置">
              <a-textarea :value="jsonConfig" :rows="8" readonly class="export-textarea" />
            </a-tab-pane>
            <a-tab-pane key="html" tab="HTML输出">
              <a-textarea :value="htmlOutput" :rows="8" readonly class="export-textarea" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { PlayCircleOutlined, RefreshOutlined, ExportOutlined } from '@ant-design/icons-vue';

const sfcCode = ref(`<template>
  <div class="demo-component">
    <a-card title="示例组件" :bordered="false">
      <a-button type="primary" @click="handleClick">点击我</a-button>
      <a-divider />
      <a-tag color="green">计数器: {{ count }}</a-tag>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const count = ref(0);

const handleClick = () => {
  count.value++;
};
</script>

<style scoped>
.demo-component {
  padding: 16px;
}
</style>`);

const compiled = ref(false);
const compileError = ref('');
const compileStatus = ref('未编译');
const compileTime = ref(0);
const previewIframe = ref<HTMLIFrameElement | null>(null);

const jsonConfig = computed(() => {
  return JSON.stringify({
    template: extractTemplate(sfcCode.value),
    script: extractScript(sfcCode.value),
    style: extractStyle(sfcCode.value),
  }, null, 2);
});

const htmlOutput = computed(() => {
  if (!compiled.value || compileError.value) return '';
  return generateHtml(sfcCode.value);
});

const extractTemplate = (code: string): string => {
  const match = code.match(/<template>([\s\S]*?)<\/template>/);
  return match ? match[1].trim() : '';
};

const extractScript = (code: string): string => {
  const match = code.match(/<script[^>]*>([\s\S]*?)<\/script>/);
  return match ? match[1].trim() : '';
};

const extractStyle = (code: string): string => {
  const match = code.match(/<style[^>]*>([\s\S]*?)<\/style>/);
  return match ? match[1].trim() : '';
};

const generateHtml = (code: string): string => {
  const template = extractTemplate(code);
  const script = extractScript(code);
  const style = extractStyle(code);
  
  return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sandbox Preview</title>
  <style>${style}</style>
  <script type="importmap">
    {
      "imports": {
        "vue": "https://unpkg.com/vue@3/dist/vue.esm-browser.js"
      }
    }
  </script>
</head>
<body>
  <div id="app">${template}</div>
  <script type="module">
    import { createApp, ref } from 'vue';
    const app = createApp({
      setup() {
        ${script.replace(/import.*from.*vue.*;/g, '')}
        return { count };
      }
    });
    app.mount('#app');
  <\/script>
</body>
</html>`;
};

const handleCompile = async () => {
  compileStatus.value = '编译中...';
  compileError.value = '';
  
  const startTime = performance.now();
  
  try {
    const html = generateHtml(sfcCode.value);
    
    if (previewIframe.value) {
      const blob = new Blob([html], { type: 'text/html' });
      const url = URL.createObjectURL(blob);
      previewIframe.value.src = url;
    }
    
    compiled.value = true;
    compileStatus.value = '编译成功';
  } catch (error) {
    compileError.value = error instanceof Error ? error.message : '未知错误';
    compileStatus.value = '编译失败';
  }
  
  compileTime.value = Math.round(performance.now() - startTime);
};

const handleReset = () => {
  compiled.value = false;
  compileError.value = '';
  compileStatus.value = '未编译';
  compileTime.value = 0;
  
  if (previewIframe.value) {
    previewIframe.value.src = '';
  }
};

const handleExport = () => {
  const html = generateHtml(sfcCode.value);
  const blob = new Blob([html], { type: 'text/html' });
  const url = URL.createObjectURL(blob);
  
  const a = document.createElement('a');
  a.href = url;
  a.download = 'component-preview.html';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
};
</script>

<style scoped>
.sandbox-preview {
  padding: 20px;
}

.sandbox-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sandbox-body {
  min-height: 500px;
}

.editor-panel,
.preview-panel {
  height: 500px;
}

.sfc-editor {
  width: 100%;
  height: 400px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
}

.preview-container {
  height: 400px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.empty-state,
.error-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.export-textarea {
  width: 100%;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
}
</style>