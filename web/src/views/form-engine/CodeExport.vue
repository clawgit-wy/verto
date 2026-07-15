<template>
  <div class="code-export">
    <!-- 顶部工具栏 -->
    <div class="export-header">
      <div class="header-left">
        <span class="header-title">代码导出</span>
        <a-tag v-if="processId" color="blue">流程ID：{{ processId }}</a-tag>
      </div>
      <div class="header-right">
        <a-button @click="handleBack">
          <template #icon><RollbackOutlined /></template>
          返回列表
        </a-button>
      </div>
    </div>

    <!-- 主体 -->
    <div class="export-body">
      <!-- 左侧：选项面板 -->
      <div class="export-left">
        <div class="panel-header">导出配置</div>
        <div class="options-area">
          <!-- UI 框架选择 -->
          <div class="opt-section">
            <div class="opt-title">UI 框架</div>
            <a-radio-group v-model:value="opts.framework" direction="vertical">
              <a-radio v-for="fw in UI_FRAMEWORKS" :key="fw.value" :value="fw.value">
                {{ fw.label }}
              </a-radio>
            </a-radio-group>
          </div>

          <!-- 代码风格选择 -->
          <div class="opt-section">
            <div class="opt-title">代码风格</div>
            <a-radio-group v-model:value="opts.style" direction="vertical">
              <a-radio v-for="cs in CODE_STYLES" :key="cs.value" :value="cs.value">
                {{ cs.label }}
              </a-radio>
            </a-radio-group>
          </div>

          <!-- 导出类型选择 -->
          <div class="opt-section">
            <div class="opt-title">导出类型</div>
            <a-radio-group v-model:value="opts.exportType" direction="vertical">
              <a-radio value="frontend">前端实现代码（Vue3 SFC）</a-radio>
              <a-radio value="backend">后端接口层代码（Java Controller）</a-radio>
              <a-radio value="both">前后端代码（同时导出）</a-radio>
            </a-radio-group>
          </div>

          <!-- 流程模型选择 -->
          <div class="opt-section">
            <div class="opt-title">BPM 流程模型</div>
            <a-select
              v-model:value="opts.modelId"
              style="width: 100%"
              placeholder="选择业务流程模型"
            >
              <a-select-option v-for="m in BPM_PROCESS_MODELS" :key="m.modelId" :value="m.modelId">
                {{ m.processName }}（{{ m.modelId }}）
              </a-select-option>
            </a-select>
            <div v-if="selectedModel" class="model-desc">
              <a-tag size="small" color="cyan">{{ selectedModel.category }}</a-tag>
              <span>{{ selectedModel.description }}</span>
            </div>
          </div>

          <!-- 选项开关 -->
          <div class="opt-section">
            <div class="opt-title">生成选项</div>
            <div class="opt-switch">
              <a-switch v-model:checked="opts.includeRules" size="small" />
              <span class="switch-label">含表单校验规则</span>
            </div>
            <div class="opt-switch">
              <a-switch v-model:checked="opts.includePermissions" size="small" />
              <span class="switch-label">含字段权限控制（auth矩阵）</span>
            </div>
            <div class="opt-switch">
              <a-switch v-model:checked="opts.includeBpm" size="small" />
              <span class="switch-label">含BPM集成代码（useBpm Hook）</span>
            </div>
            <div class="opt-switch">
              <a-switch v-model:checked="opts.includeCallbacks" size="small" />
              <span class="switch-label">含回调函数占位（doMethodProcess）</span>
            </div>
            <div class="opt-switch">
              <a-switch v-model:checked="opts.includeApproval" size="small" />
              <span class="switch-label">含审批操作按钮</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="opt-actions">
            <a-button type="primary" block @click="handleCopy">
              <template #icon><CopyOutlined /></template>
              复制代码
            </a-button>
            <a-button block @click="handleDownload">
              <template #icon><DownloadOutlined /></template>
              下载 .vue 文件
            </a-button>
          </div>
        </div>
      </div>

      <!-- 右侧：代码预览 -->
      <div class="export-right">
        <div class="panel-header">
          <span>{{ fileName }}</span>
          <a-space size="small">
            <a-tag color="green">{{ opts.framework }}</a-tag>
            <a-tag color="blue">{{ codeLineCount }} 行</a-tag>
          </a-space>
        </div>
        <div class="code-area">
          <pre class="code-block"><code v-html="highlightedCode"></code></pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  // update-begin---author:formengine ---date:2026-07-08  for：【代码导出】对齐蓝凌BPM的useBpm Hook模式生成Vue3 SFC---
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { RollbackOutlined, CopyOutlined, DownloadOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getProcessById } from '/@/api/form-engine';
  import { UI_FRAMEWORKS, CODE_STYLES, BPM_PROCESS_MODELS } from './FormEngine.data';

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();

  const processId = computed(() => (route.query.processId as string) || '');

  // ===== form-create 规则数据结构 =====
  interface FormRule {
    type: string;
    field: string;
    title: string;
    props: Record<string, any>;
    validate: any[];
    permissions: { applicant: string; manager: string; finance: string };
    nodePermissions?: Record<string, number>;
  }

  // ===== 表单 Schema =====
  const schema = ref<FormRule[]>([]);

  // ===== 导出选项 =====
  const opts = reactive({
    framework: 'antd',
    style: 'setup',
    // update-begin---author:formengine ---date:2026-07-15  for：【代码导出】增加导出类型选项(前端/后端/前后端)---
    exportType: 'frontend' as 'frontend' | 'backend' | 'both',
    // update-end---author:formengine ---date:2026-07-15  for：【代码导出】增加导出类型选项(前端/后端/前后端)---
    modelId: '',
    includeRules: true,
    includePermissions: true,
    includeBpm: true,
    includeCallbacks: true,
    includeApproval: true,
  });

  // ===== 选中的流程模型 =====
  const selectedModel = computed(() =>
    BPM_PROCESS_MODELS.find((m) => m.modelId === opts.modelId),
  );

  // ===== 文件名 =====
  // update-begin---author:formengine ---date:2026-07-15  for：【代码导出】根据导出类型调整文件名后缀---
  const fileName = computed(() => {
    const base = opts.modelId || 'Generated';
    if (opts.exportType === 'backend') {
      return `Bpm${base.charAt(0).toUpperCase() + base.slice(1)}Controller.java`;
    } else if (opts.exportType === 'both') {
      return `${base}-full-code.txt`;
    }
    return `${base}-form.vue`;
  });
  // update-end---author:formengine ---date:2026-07-15  for：【代码导出】根据导出类型调整文件名后缀---

  // ===== 获取 UI 框架配置 =====
  function getFrameworkConfig() {
    const fw = UI_FRAMEWORKS.find((f) => f.value === opts.framework) || UI_FRAMEWORKS[0];
    return {
      prefix: fw.tagPrefix,
      form: fw.formComponent,
      item: fw.itemComponent,
    };
  }

  // ===== 组件标签映射 =====
  function getFieldTag(type: string) {
    const { prefix } = getFrameworkConfig();
    const tagMap: Record<string, string> = {
      input: `${prefix}input`,
      textarea: `${prefix}textarea`,
      number: `${prefix}input-number`,
      select: `${prefix}select`,
      radio: `${prefix}radio-group`,
      checkbox: `${prefix}checkbox-group`,
      date: `${prefix}date-picker`,
      switch: `${prefix}switch`,
      button: `${prefix}button`,
    };
    return tagMap[type] || `${prefix}input`;
  }

  // ===== 生成 template 部分 =====
  // update-begin---author:formengine ---date:2026-07-08  for：【代码导出】template生成含BPM iframe/审批按钮/权限控制---
  function generateTemplate(): string {
    const { form, item, prefix } = getFrameworkConfig();
    const fields = schema.value.filter(
      (r) => r.type !== 'button' && r.type !== 'title' && r.type !== 'divider' && r.type !== 'alert',
    );
    const buttons = schema.value.filter((r) => r.type === 'button');

    let tpl = '<template>\n';
    tpl += '  <div class="form-container">\n';
    tpl += `    <${form} :model="form" ${opts.includeRules ? ':rules="rules" ' : ''}label-width="110px">\n`;

    fields.forEach((f) => {
      // 字段权限控制
      if (opts.includePermissions) {
        tpl += `      <${item} v-if="getFieldAuth('${f.field}', currentNode) !== 1" label="${f.title}" name="${f.field}">\n`;
      } else {
        tpl += `      <${item} label="${f.title}" name="${f.field}">\n`;
      }

      const tag = getFieldTag(f.type);
      const placeholder = f.props?.placeholder || '';
      // 权限控制：只读时 disabled
      const permAttr = opts.includePermissions
        ? ` :disabled="getFieldAuth('${f.field}', currentNode) === 2"`
        : '';

      if (f.type === 'textarea') {
        tpl += `        <${tag} v-model:value="form.${f.field}"${permAttr} :rows="3" placeholder="${placeholder}" />\n`;
      } else if (f.type === 'number') {
        tpl += `        <${tag} v-model:value="form.${f.field}"${permAttr} :min="0" style="width:100%" />\n`;
      } else if (f.type === 'select') {
        const options = f.props?.options || [];
        tpl += `        <${tag} v-model:value="form.${f.field}"${permAttr} placeholder="${placeholder}">\n`;
        options.forEach((o: any) => {
          tpl += `          <${prefix}option :value="${o.value}">${o.label}</${prefix}option>\n`;
        });
        tpl += `        </${tag}>\n`;
      } else if (f.type === 'switch') {
        tpl += `        <${tag} v-model:checked="form.${f.field}"${permAttr} />\n`;
      } else {
        tpl += `        <${tag} v-model:value="form.${f.field}"${permAttr} placeholder="${placeholder}" />\n`;
      }
      tpl += `      </${item}>\n`;
    });

    // 按钮组件
    if (buttons.length > 0) {
      tpl += `      <${item}>\n`;
      buttons.forEach((b) => {
        const methodKey = b.props?.methodKey || '';
        const handler = methodKey ? `onMethod_${methodKey.replace(/\./g, '_')}` : 'onSubmit';
        tpl += `        <${getFieldTag('button')} type="primary" @click="${handler}">${b.title}</${getFieldTag('button')}>\n`;
      });
      tpl += `      </${item}>\n`;
    }

    tpl += `    </${form}>\n`;

    // BPM 集成：iframe 嵌入流程图
    if (opts.includeBpm) {
      tpl += '\n    <!-- BPM 流程图区域 -->\n';
      tpl += '    <iframe v-if="isShowBpm" :src="iframeUrl" class="bpm-iframe" />\n';
    }

    // 审批操作按钮
    if (opts.includeApproval) {
      tpl += '\n    <!-- 审批操作按钮 -->\n';
      tpl += '    <div class="approval-actions">\n';
      tpl += '      <button type="primary" @click="createProcess">创建流程</button>\n';
      tpl += '      <button type="primary" @click="approveProcess(\'handler_pass\')">审批</button>\n';
      tpl += '      <button @click="approveProcess(\'handler_refuse\')">驳回</button>\n';
      tpl += '      <button danger @click="approveProcess(\'drafter_abandon\')">废弃</button>\n';
      tpl += '    </div>\n';
    }

    tpl += '  </div>\n</template>\n';
    return tpl;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【代码导出】template生成含BPM iframe/审批按钮/权限控制---

  // ===== 生成 script setup 部分 =====
  // update-begin---author:formengine ---date:2026-07-08  for：【代码导出】script生成含useBpm Hook/doMethodProcess/审批操作---
  function generateScriptSetup(): string {
    const fields = schema.value.filter(
      (r) => r.type !== 'button' && r.type !== 'title' && r.type !== 'divider' && r.type !== 'alert',
    );
    const buttons = schema.value.filter((r) => r.type === 'button');
    const modelId = opts.modelId || 'build';
    const formId = selectedModel.value?.formId || 'safety_build_process';

    let scr = '\n<script setup lang="ts">\n';
    scr += `import { reactive, ref, computed } from 'vue'\n`;

    // BPM 集成导入
    if (opts.includeBpm) {
      scr += `import { useBpm } from '/@/hooks/useBpm'\n`;
    }

    // BPM 流程模型配置
    scr += `\n// BPM 流程模型配置\n`;
    scr += `const modelId = '${modelId}'\n`;
    scr += `const formId = '${formId}'\n`;

    // useBpm Hook 初始化
    if (opts.includeBpm) {
      scr += `\n// useBpm Hook：封装BPM流程操作\n`;
      scr += `const { isShowBpm, iframeUrl, saveAndCreateProcess, approveProcess: bpmApprove, getBpmData } = useBpm({ modelId, formId })\n`;
    }

    // 表单数据模型
    scr += `\n// 表单数据模型\n`;
    scr += `const form = reactive({\n`;
    fields.forEach((f) => {
      const dv = f.type === 'number' ? 0 : "''";
      scr += `  ${f.field}: ${dv},\n`;
    });
    scr += `})\n`;

    // 当前节点
    scr += `\n// 当前流程节点\n`;
    scr += `const currentNode = ref('')\n`;

    // 校验规则
    if (opts.includeRules) {
      scr += `\n// 表单校验规则\n`;
      scr += `const rules = reactive({\n`;
      fields
        .filter((f) => f.validate?.some((v: any) => v.required))
        .forEach((f) => {
          scr += `  ${f.field}: [{ required: true, message: '${f.title}不能为空', trigger: 'blur' }],\n`;
        });
      scr += `})\n`;
    }

    // 权限矩阵计算函数
    if (opts.includePermissions) {
      scr += `\n// 权限矩阵：auth值 0=可编辑, 1=不可见, 2=只读\n`;
      scr += `const authMatrix: Record<string, Record<string, number>> = {\n`;
      fields.forEach((f) => {
        const np = f.nodePermissions || {};
        const entries = Object.keys(np).length > 0
          ? Object.entries(np).map(([k, v]) => `${k}: ${v}`).join(', ')
          : '';
        scr += `  ${f.field}: { ${entries} },\n`;
      });
      scr += `}\n\n`;
      scr += `// 获取字段在指定节点的权限值（0=可编辑, 1=不可见, 2=只读）\n`;
      scr += `function getFieldAuth(field: string, nodeId: string): number {\n`;
      scr += `  return authMatrix[field]?.[nodeId] ?? 0\n`;
      scr += `}\n`;
    }

    // doMethodProcess 占位函数
    if (opts.includeCallbacks) {
      scr += `\n// doMethodProcess 占位函数：调用后端业务回调\n`;
      scr += `async function doMethodProcess(methodKey: string, params: Record<string, any>) {\n`;
      scr += `  // TODO: 替换为真实接口调用\n`;
      scr += `  console.log('[doMethodProcess]', methodKey, params)\n`;
      scr += `  return Promise.resolve({ success: true, data: {} })\n`;
      scr += `}\n`;

      // 按钮事件处理函数
      buttons.forEach((b) => {
        const methodKey = b.props?.methodKey || '';
        const handler = methodKey ? `onMethod_${methodKey.replace(/\./g, '_')}` : 'onSubmit';
        scr += `\n// ${b.title}：绑定 ${methodKey || '提交'}\n`;
        scr += `function ${handler}() {\n`;
        if (methodKey) {
          scr += `  doMethodProcess('${methodKey}', { ...form }).then((res) => {\n`;
          scr += `    if (res.success && res.data) {\n`;
          scr += `      Object.keys(res.data).forEach((k) => { (form as any)[k] = res.data[k] })\n`;
          scr += `    }\n`;
          scr += `  })\n`;
        } else {
          scr += `  console.log('submit', form)\n`;
        }
        scr += `}\n`;
      });
    }

    // BPM 操作函数
    if (opts.includeApproval) {
      scr += `\n// 创建BPM流程\n`;
      scr += `async function createProcess() {\n`;
      if (opts.includeBpm) {
        scr += `  const result = await saveAndCreateProcess({ ...form, modelId, formId })\n`;
      } else {
        scr += `  // TODO: 调用 saveAndCreateProcess 创建BPM流程\n`;
        scr += `  console.log('createProcess', form)\n`;
      }
      scr += `  console.log('流程创建结果', result)\n`;
      scr += `}\n`;

      scr += `\n// 审批操作：handler_pass/handler_refuse/drafter_abandon\n`;
      scr += `async function approveProcess(operationType: string) {\n`;
      if (opts.includeBpm) {
        scr += `  const result = await bpmApprove({ operationType, modelId, formData: { ...form } })\n`;
      } else {
        scr += `  // TODO: 调用 BPM 审批接口\n`;
        scr += `  console.log('approveProcess', operationType, form)\n`;
      }
      scr += `  console.log('审批结果', result)\n`;
      scr += `}\n`;

      scr += `\n// 获取BPM审批数据\n`;
      scr += `async function getBpmData() {\n`;
      if (opts.includeBpm) {
        scr += `  const data = await getBpmData()\n`;
        scr += `  return data\n`;
      } else {
        scr += `  // TODO: 调用 getBpmData 获取审批数据\n`;
        scr += `  return null\n`;
      }
      scr += `}\n`;
    }

    scr += `<\/script>\n`;
    return scr;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【代码导出】script生成含useBpm Hook/doMethodProcess/审批操作---

  // ===== 生成 script options 部分 =====
  function generateScriptOptions(): string {
    const fields = schema.value.filter(
      (r) => r.type !== 'button' && r.type !== 'title' && r.type !== 'divider' && r.type !== 'alert',
    );
    const buttons = schema.value.filter((r) => r.type === 'button');
    const modelId = opts.modelId || 'build';
    const formId = selectedModel.value?.formId || 'safety_build_process';

    let scr = '\n<script lang="ts">\n';
    scr += `import { reactive, ref } from 'vue'\n`;
    if (opts.includeBpm) {
      scr += `import { useBpm } from '/@/hooks/useBpm'\n`;
    }
    scr += `\nexport default {\n`;
    scr += `  name: 'GeneratedForm',\n`;

    scr += `  setup() {\n`;
    scr += `    // BPM 流程模型配置\n`;
    scr += `    const modelId = '${modelId}'\n`;
    scr += `    const formId = '${formId}'\n`;

    if (opts.includeBpm) {
      scr += `    const { isShowBpm, iframeUrl, saveAndCreateProcess, approveProcess: bpmApprove, getBpmData } = useBpm({ modelId, formId })\n`;
    }

    scr += `    const form = reactive({\n`;
    fields.forEach((f) => {
      const dv = f.type === 'number' ? 0 : "''";
      scr += `      ${f.field}: ${dv},\n`;
    });
    scr += `    })\n`;
    scr += `    const currentNode = ref('')\n`;

    if (opts.includeRules) {
      scr += `    const rules = reactive({\n`;
      fields
        .filter((f) => f.validate?.some((v: any) => v.required))
        .forEach((f) => {
          scr += `      ${f.field}: [{ required: true, message: '${f.title}不能为空', trigger: 'blur' }],\n`;
        });
      scr += `    })\n`;
    }

    if (opts.includePermissions) {
      scr += `    const authMatrix: Record<string, Record<string, number>> = {\n`;
      fields.forEach((f) => {
        const np = f.nodePermissions || {};
        const entries = Object.keys(np).length > 0
          ? Object.entries(np).map(([k, v]) => `${k}: ${v}`).join(', ')
          : '';
        scr += `      ${f.field}: { ${entries} },\n`;
      });
      scr += `    }\n`;
      scr += `    function getFieldAuth(field: string, nodeId: string): number {\n`;
      scr += `      return authMatrix[field]?.[nodeId] ?? 0\n`;
      scr += `    }\n`;
    }

    if (opts.includeCallbacks) {
      scr += `    async function doMethodProcess(methodKey: string, params: Record<string, any>) {\n`;
      scr += `      console.log('[doMethodProcess]', methodKey, params)\n`;
      scr += `      return Promise.resolve({ success: true, data: {} })\n`;
      scr += `    }\n`;
      buttons.forEach((b) => {
        const methodKey = b.props?.methodKey || '';
        const handler = methodKey ? `onMethod_${methodKey.replace(/\./g, '_')}` : 'onSubmit';
        scr += `    function ${handler}() {\n`;
        if (methodKey) {
          scr += `      doMethodProcess('${methodKey}', { ...form }).then((res) => {\n`;
          scr += `        if (res.success && res.data) Object.keys(res.data).forEach((k) => { (form as any)[k] = res.data[k] })\n`;
          scr += `      })\n`;
        } else {
          scr += `      console.log('submit', form)\n`;
        }
        scr += `    }\n`;
      });
    }

    if (opts.includeApproval) {
      scr += `    async function createProcess() {\n`;
      if (opts.includeBpm) {
        scr += `      const result = await saveAndCreateProcess({ ...form, modelId, formId })\n`;
      } else {
        scr += `      console.log('createProcess', form)\n`;
      }
      scr += `    }\n`;
      scr += `    async function approveProcess(operationType: string) {\n`;
      if (opts.includeBpm) {
        scr += `      const result = await bpmApprove({ operationType, modelId, formData: { ...form } })\n`;
      } else {
        scr += `      console.log('approveProcess', operationType, form)\n`;
      }
      scr += `    }\n`;
    }

    scr += `    return {\n`;
    scr += `      form, currentNode,`;
    if (opts.includeRules) scr += ` rules,`;
    if (opts.includeBpm) scr += ` isShowBpm, iframeUrl,`;
    if (opts.includePermissions) scr += ` getFieldAuth,`;
    if (opts.includeCallbacks) {
      scr += ` doMethodProcess,`;
      buttons.forEach((b) => {
        const methodKey = b.props?.methodKey || '';
        const handler = methodKey ? `onMethod_${methodKey.replace(/\./g, '_')}` : 'onSubmit';
        scr += ` ${handler},`;
      });
    }
    if (opts.includeApproval) scr += ` createProcess, approveProcess,`;
    scr += `\n    }\n`;
    scr += `  },\n`;
    scr += `}\n<\/script>\n`;
    return scr;
  }

  // ===== 生成 style 部分 =====
  function generateStyle(): string {
    let sty = '\n<style scoped>\n';
    sty += `.form-container {\n  max-width: 600px;\n  margin: 0 auto;\n  padding: 20px;\n}\n`;
    if (opts.includeBpm) {
      sty += `.bpm-iframe {\n  width: 100%;\n  height: 400px;\n  border: 1px solid #e8e8e8;\n  border-radius: 4px;\n  margin-top: 16px;\n}\n`;
    }
    if (opts.includeApproval) {
      sty += `.approval-actions {\n  display: flex;\n  gap: 8px;\n  margin-top: 16px;\n  justify-content: center;\n}\n`;
    }
    sty += `<\/style>\n`;
    return sty;
  }

  // ===== 生成完整 SFC 代码 =====
  // update-begin---author:formengine ---date:2026-07-15  for：【代码导出】支持导出前端/后端/前后端代码---
  const generatedCode = computed(() => {
    if (schema.value.length === 0 && opts.exportType !== 'backend') {
      return '// 请先在设计器中配置表单Schema，或选择流程模型后生成代码';
    }
    if (opts.exportType === 'frontend') {
      const tpl = generateTemplate();
      const scr = opts.style === 'setup' ? generateScriptSetup() : generateScriptOptions();
      const sty = generateStyle();
      return tpl + scr + sty;
    } else if (opts.exportType === 'backend') {
      return generateBackendCode();
    } else {
      // both: 前端代码 + 后端代码
      const tpl = generateTemplate();
      const scr = opts.style === 'setup' ? generateScriptSetup() : generateScriptOptions();
      const sty = generateStyle();
      const frontendCode = tpl + scr + sty;
      const backendCode = generateBackendCode();
      return '/* ============ 前端实现代码（Vue3 SFC） ============ */\n\n'
        + frontendCode
        + '\n\n\n/* ============ 后端接口层代码（Java Controller） ============ */\n\n'
        + '/* 文件名: BpmFormController.java */\n'
        + backendCode;
    }
  });
  // update-end---author:formengine ---date:2026-07-15  for：【代码导出】支持导出前端/后端/前后端代码---

  // update-begin---author:formengine ---date:2026-07-15  for：【代码导出】新增后端接口层代码生成函数---
  /**
   * 生成后端接口层代码（Java Controller）
   * 包含：表单字段管理、BPM流程操作、字段权限获取、doMethodProcess回调
   */
  function generateBackendCode(): string {
    const fields = schema.value.filter(
      (r) => r.type !== 'button' && r.type !== 'title' && r.type !== 'divider' && r.type !== 'alert',
    );
    const modelId = opts.modelId || 'build';
    const formId = selectedModel.value?.formId || 'safety_build_process';
    const className = `Bpm${modelId.charAt(0).toUpperCase() + modelId.slice(1)}Controller`;

    let code = '';
    code += `package org.jeecg.modules.formengine.controller;\n\n`;
    code += `import com.alibaba.fastjson.JSON;\n`;
    code += `import com.alibaba.fastjson.JSONObject;\n`;
    code += `import io.swagger.v3.oas.annotations.Operation;\n`;
    code += `import io.swagger.v3.oas.annotations.tags.Tag;\n`;
    code += `import lombok.extern.slf4j.Slf4j;\n`;
    code += `import org.jeecg.common.api.vo.Result;\n`;
    code += `import org.jeecg.common.aspect.annotation.AutoLog;\n`;
    code += `import org.springframework.web.bind.annotation.*;\n\n`;
    code += `import java.util.*;\n\n`;
    code += `/**\n`;
    code += ` * BPM表单接口层 - ${selectedModel.value?.processName || modelId}\n`;
    code += ` * 自动生成于 ${new Date().toLocaleString()}\n`;
    code += ` * 对接蓝凌BPM平台，提供表单字段管理、流程操作、回调执行等接口\n`;
    code += ` */\n`;
    code += `@Tag(name = "BPM表单接口-${modelId}")\n`;
    code += `@RestController\n`;
    code += `@RequestMapping("/formengine/bpm/${modelId}")\n`;
    code += `@Slf4j\n`;
    code += `public class ${className} {\n\n`;

    // 1. 获取表单字段定义
    code += `    /**\n`;
    code += `     * 获取表单字段定义（对应BPM对接接口1）\n`;
    code += `     * 返回该流程的表单字段Schema，供BPM平台绑定表单使用\n`;
    code += `     */\n`;
    code += `    @Operation(summary = "获取表单字段定义")\n`;
    code += `    @GetMapping("/formFields")\n`;
    code += `    public Result<List<Map<String, Object>>> getFormFields() {\n`;
    code += `        log.info("[${modelId}] 获取表单字段定义");\n`;
    code += `        List<Map<String, Object>> fields = new ArrayList<>();\n`;
    fields.forEach((f) => {
      code += `        {\n`;
      code += `            Map<String, Object> field = new LinkedHashMap<>();\n`;
      code += `            field.put("fieldKey", "${f.field}");\n`;
      code += `            field.put("fieldLabel", "${f.title}");\n`;
      code += `            field.put("fieldType", "${mapCompTypeToFieldType(f.type)}");\n`;
      code += `            field.put("required", ${f.validate?.some((v: any) => v.required) ? 'true' : 'false'});\n`;
      code += `            fields.add(field);\n`;
      code += `        }\n`;
    });
    code += `        return Result.OK(fields);\n`;
    code += `    }\n\n`;

    // 2. 获取表单字段值
    code += `    /**\n`;
    code += `     * 获取表单字段值（对应BPM对接接口4）\n`;
    code += `     * 在流程运行过程中，根据节点和角色返回表单字段值及权限\n`;
    code += `     */\n`;
    code += `    @Operation(summary = "获取表单字段值")\n`;
    code += `    @GetMapping("/formValues")\n`;
    code += `    public Result<Map<String, Object>> getFormValues(\n`;
    code += `            @RequestParam(name = "processInstanceId") String processInstanceId,\n`;
    code += `            @RequestParam(name = "nodeId", required = false) String nodeId,\n`;
    code += `            @RequestParam(name = "role", required = false) String role) {\n`;
    code += `        log.info("[${modelId}] 获取表单字段值: processInstanceId={}, nodeId={}, role={}", processInstanceId, nodeId, role);\n`;
    code += `        Map<String, Object> formValues = new LinkedHashMap<>();\n`;
    fields.forEach((f) => {
      const dv = f.type === 'number' ? '0' : '""';
      code += `        formValues.put("${f.field}", ${dv});\n`;
    });
    code += `        // TODO: 根据nodeId和role设置字段权限（auth: 0=可编辑, 1=不可见, 2=只读）\n`;
    code += `        Map<String, Integer> fieldAuth = new LinkedHashMap<>();\n`;
    fields.forEach((f) => {
      code += `        fieldAuth.put("${f.field}", 0);\n`;
    });
    code += `        formValues.put("_fieldAuth", fieldAuth);\n`;
    code += `        return Result.OK(formValues);\n`;
    code += `    }\n\n`;

    // 3. 流程操作
    if (opts.includeApproval) {
      code += `    /**\n`;
      code += `     * BPM流程操作（5种操作类型）\n`;
      code += `     * handler_pass=审批通过, handler_refuse=驳回, drafter_abandon=废弃,\n`;
      code += `     * handler_assign=指派, handler_communicate=沟通\n`;
      code += `     */\n`;
      code += `    @AutoLog(value = "BPM流程操作")\n`;
      code += `    @Operation(summary = "BPM流程操作")\n`;
      code += `    @PostMapping("/operate")\n`;
      code += `    public Result<Map<String, Object>> operateProcess(@RequestBody Map<String, Object> body) {\n`;
      code += `        String operationType = (String) body.get("operationType");\n`;
      code += `        String processInstanceId = (String) body.get("processInstanceId");\n`;
      code += `        String nodeId = (String) body.get("nodeId");\n`;
      code += `        log.info("[${modelId}] 流程操作: operationType={}, processInstanceId={}, nodeId={}", operationType, processInstanceId, nodeId);\n`;
      code += `        Map<String, Object> result = new LinkedHashMap<>();\n`;
      code += `        result.put("success", true);\n`;
      code += `        result.put("operationType", operationType);\n`;
      code += `        result.put("timestamp", new Date());\n`;
      code += `        // TODO: 调用BPM平台接口执行流程操作\n`;
      code += `        return Result.OK(result);\n`;
      code += `    }\n\n`;
    }

    // 4. 创建流程
    if (opts.includeBpm) {
      code += `    /**\n`;
      code += `     * 创建BPM流程\n`;
      code += `     */\n`;
      code += `    @AutoLog(value = "创建BPM流程")\n`;
      code += `    @Operation(summary = "创建BPM流程")\n`;
      code += `    @PostMapping("/createProcess")\n`;
      code += `    public Result<Map<String, Object>> createProcess(@RequestBody Map<String, Object> body) {\n`;
      code += `        log.info("[${modelId}] 创建BPM流程: modelId=${modelId}, formId=${formId}");\n`;
      code += `        Map<String, Object> result = new LinkedHashMap<>();\n`;
      code += `        result.put("success", true);\n`;
      code += `        result.put("processInstanceId", "pi_" + System.currentTimeMillis());\n`;
      code += `        result.put("modelId", "${modelId}");\n`;
      code += `        result.put("formId", "${formId}");\n`;
      code += `        result.put("timestamp", new Date());\n`;
      code += `        // TODO: 调用BPM平台接口创建流程实例\n`;
      code += `        return Result.OK(result);\n`;
      code += `    }\n\n`;
    }

    // 5. doMethodProcess 回调
    if (opts.includeCallbacks) {
      code += `    /**\n`;
      code += `     * doMethodProcess 业务回调执行\n`;
      code += `     * 流程运行过程中触发的业务回调，远程调用业务系统服务\n`;
      code += `     */\n`;
      code += `    @AutoLog(value = "业务回调执行")\n`;
      code += `    @Operation(summary = "doMethodProcess业务回调")\n`;
      code += `    @PostMapping("/doMethodProcess")\n`;
      code += `    public Result<Map<String, Object>> doMethodProcess(@RequestBody Map<String, Object> body) {\n`;
      code += `        String methodKey = (String) body.get("methodKey");\n`;
      code += `        log.info("[${modelId}] 业务回调执行: methodKey={}", methodKey);\n`;
      code += `        Map<String, Object> result = new LinkedHashMap<>();\n`;
      code += `        result.put("success", true);\n`;
      code += `        result.put("methodKey", methodKey);\n`;
      code += `        result.put("timestamp", new Date());\n`;
      code += `        // TODO: 根据methodKey调用对应的业务服务\n`;
      code += `        return Result.OK(result);\n`;
      code += `    }\n\n`;
    }

    // 6. 获取字段权限
    if (opts.includePermissions) {
      code += `    /**\n`;
      code += `     * 获取字段权限矩阵\n`;
      code += `     * auth值: 0=可编辑, 1=不可见, 2=只读\n`;
      code += `     */\n`;
      code += `    @Operation(summary = "获取字段权限矩阵")\n`;
      code += `    @GetMapping("/fieldAuth")\n`;
      code += `    public Result<Map<String, Map<String, Integer>>> getFieldAuth(\n`;
      code += `            @RequestParam(name = "processInstanceId") String processInstanceId,\n`;
      code += `            @RequestParam(name = "nodeId") String nodeId) {\n`;
      code += `        log.info("[${modelId}] 获取字段权限: processInstanceId={}, nodeId={}", processInstanceId, nodeId);\n`;
      code += `        Map<String, Map<String, Integer>> authMatrix = new LinkedHashMap<>();\n`;
      fields.forEach((f) => {
        const np = f.nodePermissions || {};
        code += `        {\n`;
        code += `            Map<String, Integer> auth = new LinkedHashMap<>();\n`;
        if (Object.keys(np).length > 0) {
          Object.entries(np).forEach(([k, v]) => {
            code += `            auth.put("${k}", ${v});\n`;
          });
        }
        code += `            authMatrix.put("${f.field}", auth);\n`;
        code += `        }\n`;
      });
      code += `        return Result.OK(authMatrix);\n`;
      code += `    }\n\n`;
    }

    code += `}\n`;
    return code;
  }

  /** 组件类型 -> 业务字段类型映射 */
  function mapCompTypeToFieldType(compType: string): string {
    const map: Record<string, string> = {
      input: 'string',
      textarea: 'string',
      number: 'number',
      select: 'stringArray',
      radio: 'stringArray',
      checkbox: 'stringArray',
      date: 'date',
      switch: 'boolean',
    };
    return map[compType] || 'string';
  }
  // update-end---author:formengine ---date:2026-07-15  for：【代码导出】新增后端接口层代码生成函数---

  // ===== 代码行数统计 =====
  const codeLineCount = computed(() => generatedCode.value.split('\n').length);

  // ===== 简易语法高亮 =====
  function highlight(code: string): string {
    let s = code.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    // 注释
    s = s.replace(/(\/\/[^\n]*)/g, '<span class="tok-com">$1</span>');
    // 字符串
    s = s.replace(/('[^']*')/g, '<span class="tok-str">$1</span>');
    // 关键字
    s = s.replace(
      /\b(import|from|const|let|function|return|export|default|reactive|computed|async|await|if|else|new|Promise|resolve|true|false|null|undefined|ref)\b/g,
      '<span class="tok-kw">$1</span>',
    );
    // 数字
    s = s.replace(/\b(\d+)\b/g, '<span class="tok-num">$1</span>');
    // HTML 标签
    s = s.replace(/(&lt;\/?)([a-zA-Z-]+)/g, '$1<span class="tok-tag">$2</span>');
    // 属性
    s = s.replace(/(\s)([@v:a-zA-Z-]+)(=)/g, '$1<span class="tok-attr">$2</span>$3');
    return s;
  }

  const highlightedCode = computed(() => highlight(generatedCode.value));

  // ===== 复制代码 =====
  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(generatedCode.value);
      createMessage.success('已复制代码到剪贴板');
    } catch {
      // 降级方案
      const ta = document.createElement('textarea');
      ta.value = generatedCode.value;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      document.body.removeChild(ta);
      createMessage.success('已复制代码到剪贴板');
    }
  }

  // ===== 下载 .vue 文件 =====
  function handleDownload() {
    const blob = new Blob([generatedCode.value], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName.value;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    createMessage.success(`已下载 ${fileName.value}`);
  }

  // ===== 返回列表 =====
  function handleBack() {
    router.push('/form-engine/list');
  }

  // ===== 加载流程 Schema =====
  async function loadProcess() {
    if (!processId.value) return;
    try {
      const data: any = await getProcessById(processId.value);
      const formSchema = data?.formSchema || data?.result?.formSchema;
      if (formSchema) {
        const layout = Array.isArray(formSchema) ? formSchema : formSchema.layout;
        if (Array.isArray(layout) && layout.length) {
          schema.value = layout.map((rule: any) => ({
            type: rule.type || 'input',
            field: rule.field || '',
            title: rule.title || '',
            props: rule.props || {},
            validate: rule.validate || [],
            permissions: {
              applicant: rule.permissions?.applicant || 'write',
              manager: rule.permissions?.manager || 'readonly',
              finance: rule.permissions?.finance || 'readonly',
            },
            nodePermissions: rule.nodePermissions || {},
          }));
        }
      }
      // 自动选中流程模型
      const modelId = data?.modelId || data?.result?.modelId;
      if (modelId) {
        opts.modelId = modelId;
      }
    } catch (err) {
      console.error('[form-engine] getProcessById failed', err);
    }
  }

  onMounted(loadProcess);
  // update-end---author:formengine ---date:2026-07-08  for：【代码导出】对齐蓝凌BPM的useBpm Hook模式生成Vue3 SFC---
</script>

<style scoped>
  .code-export {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f0f2f5;
  }

  .export-header {
    height: 56px;
    background: #fff;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    flex-shrink: 0;
  }
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .header-title {
    font-size: 16px;
    font-weight: 600;
  }
  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .export-body {
    flex: 1;
    display: flex;
    overflow: hidden;
  }

  .export-left {
    width: 320px;
    background: #fff;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
  }
  .panel-header {
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;
  }
  .options-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }
  .opt-section {
    margin-bottom: 24px;
    padding-bottom: 20px;
    border-bottom: 1px solid #f5f5f5;
  }
  .opt-section:last-of-type {
    border-bottom: none;
  }
  .opt-title {
    font-size: 13px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
  }
  .opt-switch {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }
  .switch-label {
    font-size: 13px;
    color: #555;
  }
  .model-desc {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .opt-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
  }

  .export-right {
    flex: 1;
    background: #fff;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .code-area {
    flex: 1;
    overflow: hidden;
    padding: 16px;
  }
  .code-block {
    margin: 0;
    background: #1e1e2e;
    color: #e5e7eb;
    font-family: 'SF Mono', Menlo, Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    padding: 18px 20px;
    border-radius: 6px;
    overflow: auto;
    height: 100%;
    white-space: pre-wrap;
    word-break: break-word;
  }

  :deep(.tok-tag) { color: #7dd3fc; }
  :deep(.tok-attr) { color: #c4b5fd; }
  :deep(.tok-str) { color: #86efac; }
  :deep(.tok-com) { color: #6b7280; font-style: italic; }
  :deep(.tok-kw) { color: #f0abfc; }
  :deep(.tok-num) { color: #fcd34d; }
</style>
