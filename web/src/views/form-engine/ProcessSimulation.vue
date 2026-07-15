<template>
  <div class="process-simulation">
    <!-- 顶部工具栏 -->
    <div class="sim-header">
      <div class="header-left">
        <span class="header-title">流程仿真测试</span>
        <a-tag v-if="processId" color="blue">流程ID：{{ processId }}</a-tag>
        <a-tag :color="statusInfo.color">{{ statusInfo.label }}</a-tag>
      </div>
      <div class="header-center">
        <span class="ctx-label">业务流程模型：</span>
        <a-select
          v-model:value="currentModelId"
          style="width: 280px"
          placeholder="选择业务流程模型"
          @change="onModelChange"
        >
          <a-select-option v-for="m in BPM_PROCESS_MODELS" :key="m.modelId" :value="m.modelId">
            {{ m.processName }}（{{ m.modelId }}）
          </a-select-option>
        </a-select>
      </div>
      <div class="header-right">
        <a-button @click="handleBack">
          <template #icon><RollbackOutlined /></template>
          返回列表
        </a-button>
      </div>
    </div>

    <!-- 主区域 -->
    <div class="sim-body">
      <!-- 左侧：表单预览区域 -->
      <div class="sim-left">
        <div class="panel-header">
          <span>表单预览（{{ currentRoleLabel }}视角）</span>
        </div>
        <div class="role-switcher">
          <span class="role-label">当前角色：</span>
          <a-radio-group
            v-if="nodeRoles.length > 0"
            v-model:value="currentRole"
            button-style="solid"
            size="small"
            @change="onRoleChange"
          >
            <a-radio-button v-for="role in nodeRoles" :key="role" :value="role">
              {{ role }}
            </a-radio-button>
          </a-radio-group>
          <span v-else class="role-empty">暂无节点角色</span>
        </div>
        <div class="form-area">
          <a-card v-if="visibleFields.length === 0" :bordered="false">
            <a-empty description="暂无可显示字段，请确认角色或流程节点" />
          </a-card>
          <a-form v-else layout="vertical">
            <a-form-item v-for="field in visibleFields" :key="field.key" :label="field.label">
              <template #label>
                <span>{{ field.label }}</span>
                <a-tag v-if="field.perm === 'write'" size="small" color="green">可编辑</a-tag>
                <a-tag v-else-if="field.perm === 'readonly'" size="small" color="blue">只读</a-tag>
              </template>
              <!-- 可编辑字段渲染 -->
              <a-textarea
                v-if="field.perm === 'write' && field.type === 'textarea'"
                v-model:value="formData[field.key]"
                :rows="3"
                :placeholder="field.placeholder || ''"
              />
              <a-input-number
                v-else-if="field.perm === 'write' && field.type === 'number'"
                v-model:value="formData[field.key]"
                style="width: 100%"
              />
              <a-select
                v-else-if="field.perm === 'write' && field.type === 'select'"
                v-model:value="formData[field.key]"
                :options="field.options || []"
                :placeholder="field.placeholder || '请选择'"
              />
              <a-switch
                v-else-if="field.perm === 'write' && field.type === 'switch'"
                v-model:checked="formData[field.key]"
              />
              <a-input
                v-else-if="field.perm === 'write'"
                v-model:value="formData[field.key]"
                :placeholder="field.placeholder || ''"
              />
              <!-- 只读字段渲染 -->
              <a-textarea
                v-else-if="field.type === 'textarea'"
                :value="formData[field.key]"
                :rows="3"
                disabled
              />
              <a-input-number
                v-else-if="field.type === 'number'"
                :value="formData[field.key]"
                style="width: 100%"
                disabled
              />
              <a-input v-else :value="formData[field.key]" disabled />
            </a-form-item>
          </a-form>
        </div>
      </div>

      <!-- 右侧：仿真操作面板 -->
      <div class="sim-right">
        <!-- 区域1：流程状态流转 -->
        <a-card title="流程状态流转" size="small" class="ops-card">
          <div class="status-row">
            <span class="row-label">当前状态：</span>
            <a-tag :color="statusInfo.color">{{ statusInfo.label }}</a-tag>
          </div>
          <div class="status-row">
            <span class="row-label">当前节点：</span>
            <a-tag v-if="currentNode" color="processing">
              {{ currentNode.name }}（{{ currentNode.nodeId }}）
            </a-tag>
            <span v-else class="text-muted">-</span>
          </div>
          <a-divider style="margin: 8px 0" />
          <!-- 审批操作按钮组 -->
          <div class="op-btn-group">
            <a-button
              v-for="op in BPM_OPERATION_TYPES"
              :key="op.value"
              :type="op.value === 'handler_pass' ? 'primary' : 'default'"
              size="small"
              block
              :disabled="isOpDisabled(op.value)"
              class="op-btn"
              @click="handleOperation(op.value)"
            >
              {{ op.label }}
            </a-button>
          </div>
          <div class="op-desc">
            <span v-if="currentOpDesc" class="text-muted">{{ currentOpDesc }}</span>
          </div>
        </a-card>

        <!-- 区域2：BPM回调模拟 -->
        <a-card title="BPM回调模拟" size="small" class="ops-card">
          <div class="callback-list">
            <a-button
              v-for="cb in modelCallbacks"
              :key="cb.functionId"
              size="small"
              block
              :loading="callbackLoading === cb.functionId"
              class="op-btn"
              @click="executeCallback(cb)"
            >
              {{ cb.functionName }}
            </a-button>
            <a-empty
              v-if="modelCallbacks.length === 0"
              description="当前流程无关联回调"
              :image="simpleImage"
            />
          </div>
          <!-- 回调执行结果 -->
          <a-alert
            v-if="lastCallbackResult"
            :message="lastCallbackResult.success ? '回调执行成功' : '回调执行失败'"
            :type="lastCallbackResult.success ? 'success' : 'error'"
            show-icon
            style="margin-top: 8px"
            closable
            @close="lastCallbackResult = null"
          />
          <!-- 回调执行记录 -->
          <div v-if="callbackRecords.length > 0" class="callback-records">
            <div class="record-title">执行记录：</div>
            <div v-for="(rec, idx) in callbackRecords" :key="idx" class="record-item">
              <span class="record-time">{{ rec.time }}</span>
              <span class="record-fn">{{ rec.functionId }}</span>
              <a-tag :color="rec.success ? 'green' : 'red'" size="small">
                {{ rec.success ? '成功' : '失败' }}
              </a-tag>
            </div>
          </div>
        </a-card>

        <!-- 区域3：仿真轨迹日志 -->
        <a-card size="small" class="ops-card ops-trace">
          <template #title>
            <span>仿真轨迹日志</span>
          </template>
          <template #extra>
            <a-space size="small">
              <a-button size="small" type="link" :disabled="trace.length === 0" @click="clearTrace">
                清空
              </a-button>
            </a-space>
          </template>
          <div class="trace-list">
            <div v-if="trace.length === 0" class="trace-empty">暂无操作记录</div>
            <div
              v-for="(item, index) in trace"
              :key="index"
              class="trace-item"
              :class="{ 'replay-active': replayIndex === index }"
              @click="replayStep(index)"
            >
              <div class="trace-head">
                <span class="trace-step">#{{ item.step }}</span>
                <span class="trace-node">{{ item.nodeName }}（{{ item.nodeId }}）</span>
                <span class="trace-time">{{ item.time }}</span>
              </div>
              <div class="trace-action">
                <a-tag size="small" :color="getOpColor(item.operation)">{{ item.operationLabel }}</a-tag>
                <span class="trace-snapshot">{{ item.snapshot }}</span>
              </div>
            </div>
          </div>
        </a-card>
      </div>
    </div>

    <!-- update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】集成BPMN流程图展示区域---
    BPMN 流程图（从 process-3.xml 解析的节点和连线渲染 SVG 流程图） -->
    <div v-if="processDefXml" class="sim-flowchart">
      <div class="flowchart-header" @click="flowchartCollapsed = !flowchartCollapsed">
        <span class="flowchart-title">
          <ApartmentOutlined /> BPMN 流程图（{{ nodes.length }} 个节点）
        </span>
        <span v-if="currentNode" class="flowchart-current">
          当前节点：<a-tag color="blue" size="small">{{ currentNode.name }}（{{ currentNode.nodeId }}）</a-tag>
        </span>
        <DownOutlined :class="{ collapsed: flowchartCollapsed }" />
      </div>
      <div v-show="!flowchartCollapsed" class="flowchart-body">
        <BpmnFlowchart
          :xml-content="processDefXml"
          :current-node-id="currentNodeId"
          :passed-node-ids="passedNodeIds"
          style="height: 420px"
          @node-click="onFlowchartNodeClick"
        />
      </div>
    </div>
    <!-- update-end---author:formengine ---date:2026-07-08  for：【流程仿真】集成BPMN流程图展示区域---

    <!-- 底部：数据预览 -->
    <div class="sim-footer">
      <div class="footer-section">
        <div class="footer-header" @click="formDataCollapsed = !formDataCollapsed">
          <span>当前表单数据 JSON</span>
          <DownOutlined :class="{ collapsed: formDataCollapsed }" />
        </div>
        <pre v-show="!formDataCollapsed" class="json-preview">{{ formDataJson }}</pre>
      </div>
      <div class="footer-section">
        <div class="footer-header" @click="ctxCollapsed = !ctxCollapsed">
          <span>当前流程上下文 JSON</span>
          <DownOutlined :class="{ collapsed: ctxCollapsed }" />
        </div>
        <pre v-show="!ctxCollapsed" class="json-preview">{{ contextJson }}</pre>
      </div>
    </div>

    <!-- 转派/传阅弹窗（全屏 BasicModal + BasicForm） -->
    <BasicModal
      @register="registerAssignModal"
      :title="assignModalTitle"
      :defaultFullscreen="true"
      @ok="handleAssignSubmit"
    >
      <BasicForm @register="registerAssignForm" />
    </BasicModal>

    <!-- 回调执行结果全屏弹窗 -->
    <BasicModal
      @register="registerResultModal"
      title="回调执行结果"
      :defaultFullscreen="true"
      :showOkBtn="false"
      cancelText="关闭"
    >
      <a-alert
        v-if="lastCallbackResult"
        :message="lastCallbackResult.success ? '执行成功' : '执行失败'"
        :type="lastCallbackResult.success ? 'success' : 'error'"
        show-icon
        style="margin-bottom: 16px"
      />
      <pre class="result-preview">{{ callbackResultText }}</pre>
    </BasicModal>
  </div>
</template>

<script setup lang="ts">
  // update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】对齐蓝凌BPM业务实际流程的仿真模拟---
  import { computed, onMounted, reactive, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { Empty } from 'ant-design-vue';
  import { RollbackOutlined, DownOutlined, ApartmentOutlined } from '@ant-design/icons-vue';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { FormSchema } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  // update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】集成BPMN流程图组件---
  import BpmnFlowchart from './components/BpmnFlowchart.vue';
  // update-end---author:formengine ---date:2026-07-08  for：【流程仿真】集成BPMN流程图组件---
  import {
    getProcessById,
    getFormFieldValueList,
    doMethodProcess,
  } from '/@/api/form-engine';
  import {
    BPM_PROCESS_MODELS,
    BPM_CALLBACKS,
    BPM_OPERATION_TYPES,
    PROCESS_STATUS,
  } from './FormEngine.data';

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();
  const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;

  const processId = computed(() => (route.query.processId as string) || '');
  const templateId = computed(() => (route.query.templateId as string) || '');

  // ===== 流程模型选择 =====
  const currentModelId = ref<string>('');
  const currentModel = computed(() =>
    BPM_PROCESS_MODELS.find((m) => m.modelId === currentModelId.value),
  );

  // ===== 流程状态 =====
  const flowStatus = ref<string>('10');
  const statusInfo = computed(() => PROCESS_STATUS[flowStatus.value] || PROCESS_STATUS['10']);

  // ===== 流程节点 =====
  interface FlowNode {
    nodeId: string;
    name: string;
    role?: string;
    nodeType?: string;
    handlerNames?: string;
  }
  const nodes = ref<FlowNode[]>([]);
  const currentNodeIndex = ref(0);
  const currentNode = computed(() => nodes.value[currentNodeIndex.value] || null);

  // update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图相关状态---
  /** 流程定义 XML 内容（用于 BpmnFlowchart 组件渲染） */
  const processDefXml = ref<string>('');
  /** 当前节点ID（传给 BpmnFlowchart 高亮） */
  const currentNodeId = computed(() => currentNode.value?.nodeId || '');
  /** 已流转节点ID列表（根据轨迹日志计算） */
  const passedNodeIds = computed(() => {
    // 当前节点及之前已流转的节点
    const passed: string[] = [];
    if (nodes.value.length === 0) return passed;
    // 从轨迹日志中提取已流转的节点
    trace.value.forEach((t) => {
      if (t.nodeId && !passed.includes(t.nodeId)) {
        passed.push(t.nodeId);
      }
    });
    return passed;
  });
  /** 流程图折叠状态 */
  const flowchartCollapsed = ref(false);
  // update-end---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图相关状态---

  // ===== 角色状态（从流程节点 handlerNames 解析） =====
  const nodeRoles = computed(() => {
    const roles: string[] = [];
    nodes.value.forEach((n) => {
      if (n.handlerNames && !roles.includes(n.handlerNames)) {
        roles.push(n.handlerNames);
      }
    });
    return roles;
  });
  const currentRole = ref<string>('');
  const currentRoleLabel = computed(() => currentRole.value || '默认');

  // ===== 表单字段与权限 =====
  interface SimField {
    key: string;
    label: string;
    type: string;
    perm: string;
    placeholder?: string;
    options?: any[];
  }
  const fields = ref<SimField[]>([]);
  const formData = reactive<Record<string, any>>({});
  const visibleFields = computed(() => fields.value.filter((f) => f.perm !== 'hidden'));

  // ===== 回调模拟 =====
  const modelCallbacks = computed(() =>
    BPM_CALLBACKS.filter((cb) => cb.modelId === currentModelId.value),
  );
  const callbackLoading = ref<string>('');
  const lastCallbackResult = ref<any>(null);
  const callbackRecords = ref<{ time: string; functionId: string; success: boolean }[]>([]);

  // ===== 轨迹日志 =====
  interface TraceItem {
    step: number;
    nodeId: string;
    nodeName: string;
    operation: string;
    operationLabel: string;
    time: string;
    snapshot: string;
    formDataSnapshot: Record<string, any>;
  }
  const trace = ref<TraceItem[]>([]);
  const stepCounter = ref(0);
  const replayIndex = ref(-1);

  // ===== 当前操作描述 =====
  const currentOpDesc = ref<string>('');

  // ===== JSON 折叠 =====
  const formDataCollapsed = ref(false);
  const ctxCollapsed = ref(false);
  const formDataJson = computed(() => JSON.stringify(formData, null, 2));
  const contextJson = computed(() =>
    JSON.stringify(
      {
        modelId: currentModelId.value,
        formId: currentModel.value?.formId || '',
        processId: processId.value,
        status: flowStatus.value,
        currentNode: currentNode.value
          ? { nodeId: currentNode.value.nodeId, name: currentNode.value.name }
          : null,
      },
      null,
      2,
    ),
  );

  // ===== 弹窗 =====
  const [registerResultModal, { openModal: openResultModal }] = useModal();
  const [registerAssignModal, { openModal: openAssignModal, closeModal: closeAssignModal }] =
    useModal();
  const callbackResultText = computed(() => JSON.stringify(lastCallbackResult.value, null, 2));

  // 转派/传阅表单
  const assignType = ref<'handler_assign' | 'handler_communicate'>('handler_assign');
  const assignModalTitle = computed(() =>
    assignType.value === 'handler_assign' ? '转派流程' : '传阅流程',
  );
  const assignFormSchema: FormSchema[] = [
    {
      field: 'targetUser',
      label: '目标人员',
      component: 'Input',
      required: true,
      componentProps: {
        placeholder: '请输入人员姓名',
      },
      colProps: { span: 24 },
    },
    {
      field: 'remark',
      label: '备注',
      component: 'InputTextArea',
      componentProps: { placeholder: '请输入备注信息', rows: 3 },
      colProps: { span: 24 },
    },
  ];
  const [registerAssignForm, { resetFields: resetAssignForm, validate: validateAssignForm }] =
    useForm({
      labelWidth: 100,
      schemas: assignFormSchema,
      showActionButtonGroup: false,
    });

  // ===== 工具函数 =====
  function nowTime() {
    return new Date().toLocaleTimeString('zh-CN', { hour12: false });
  }

  function getOpColor(operation: string): string {
    const map: Record<string, string> = {
      handler_pass: 'green',
      handler_refuse: 'orange',
      drafter_abandon: 'red',
      handler_assign: 'blue',
      handler_communicate: 'cyan',
      init: 'default',
      role_switch: 'purple',
    };
    return map[operation] || 'default';
  }

  function getOpLabel(operation: string): string {
    const found = BPM_OPERATION_TYPES.find((o) => o.value === operation);
    return found ? found.label : operation;
  }

  function snapshotSummary(): string {
    const keys = Object.keys(formData);
    if (keys.length === 0) return '{}';
    const summary: Record<string, any> = {};
    keys.slice(0, 3).forEach((k) => {
      summary[k] = formData[k];
    });
    return JSON.stringify(summary);
  }

  // ===== 记录轨迹 =====
  function pushTrace(operation: string, operationLabel?: string) {
    stepCounter.value += 1;
    const node = currentNode.value;
    trace.value.unshift({
      step: stepCounter.value,
      nodeId: node?.nodeId || '-',
      nodeName: node?.name || '-',
      operation,
      operationLabel: operationLabel || getOpLabel(operation),
      time: nowTime(),
      snapshot: snapshotSummary(),
      formDataSnapshot: JSON.parse(JSON.stringify(formData)),
    });
  }

  // update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图节点点击事件---
  /** 点击流程图节点：跳转到该节点（仿真模式下用于切换当前节点查看） */
  function onFlowchartNodeClick(node: any) {
    const idx = nodes.value.findIndex((n) => n.nodeId === node.id);
    if (idx >= 0 && idx !== currentNodeIndex.value) {
      currentNodeIndex.value = idx;
      // 切换角色为该节点处理人
      const n = nodes.value[idx];
      if (n?.handlerNames) {
        currentRole.value = n.handlerNames;
      }
      createMessage.info(`已切换到节点：${node.name}（${node.id}）`);
    }
  }
  // update-end---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图节点点击事件---

  // ===== 解析蓝凌BPM流程节点 =====
  // update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】parseXmlNodes 解析蓝凌BPM节点（7种类型）---
  const LANDRAY_NODE_LABELS: Record<string, string> = {
    startNode: '开始节点',
    draftNode: '起草节点',
    reviewNode: '审批节点',
    autoBranchNode: '条件分支',
    splitNode: '并行分支',
    joinNode: '汇合节点',
    endNode: '结束节点',
  };

  function parseXmlNodes(xmlText: string): FlowNode[] {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xmlText, 'text/xml');
    const parseError = doc.querySelector('parsererror');
    if (parseError) {
      throw new Error('XML 格式错误：' + parseError.textContent?.substring(0, 100));
    }
    const result: FlowNode[] = [];
    const landrayTypes = Object.keys(LANDRAY_NODE_LABELS);
    const landrayElements: Element[] = [];
    landrayTypes.forEach((tag) => {
      const els = doc.querySelectorAll(tag);
      els.forEach((el) => landrayElements.push(el));
    });
    landrayElements.forEach((el) => {
      const nodeId = el.getAttribute('id') || el.getAttribute('nodeId') || '';
      const name = el.getAttribute('name') || el.getAttribute('title') || '';
      const tag = el.tagName;
      const handlerNames = el.getAttribute('handlerNames') || undefined;
      if (nodeId) {
        result.push({
          nodeId,
          name: name || LANDRAY_NODE_LABELS[tag] || nodeId,
          role: handlerNames || undefined,
          nodeType: tag,
          handlerNames,
        });
      }
    });
    if (result.length === 0) {
      throw new Error(
        '未找到任何蓝凌BPM节点元素（startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode）',
      );
    }
    return result;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【流程仿真】parseXmlNodes 解析蓝凌BPM节点（7种类型）---

  // ===== 加载流程数据 =====
  async function loadProcess() {
    if (!processId.value) return;
    try {
      const data: any = await getProcessById(processId.value);
      // 从 processDef（XML）解析蓝凌BPM节点
      const processDef = data?.processDef || data?.result?.processDef;
      if (processDef && typeof processDef === 'string' && processDef.trim()) {
        // 保存 XML 内容供 BpmnFlowchart 渲染
        processDefXml.value = processDef;
        try {
          const parsedNodes = parseXmlNodes(processDef);
          nodes.value = parsedNodes;
          // 初始化当前节点为第一个 draftNode
          const draftIdx = parsedNodes.findIndex((n) => n.nodeType === 'draftNode');
          currentNodeIndex.value = draftIdx >= 0 ? draftIdx : 0;
          // 初始化角色为当前节点 handlerNames
          const node = nodes.value[currentNodeIndex.value];
          if (node?.handlerNames) {
            currentRole.value = node.handlerNames;
          } else if (nodeRoles.value.length > 0) {
            currentRole.value = nodeRoles.value[0];
          }
        } catch (e) {
          console.warn('[form-engine] parseXmlNodes failed', e);
        }
      }
      // 如果流程数据携带 modelId，自动选中
      const modelId = data?.modelId || data?.result?.modelId;
      if (modelId) {
        currentModelId.value = modelId;
      }
    } catch (err) {
      console.error('[form-engine] getProcessById failed', err);
    }
  }

  // ===== 加载字段与权限 =====
  async function loadFields() {
    const tid = templateId.value;
    if (!tid || !currentRole.value) return;
    try {
      const data: any = await getFormFieldValueList(tid, currentRole.value);
      const resp = data?.values !== undefined ? data : data?.result;
      if (resp) {
        // 回写表单值
        if (resp.values && typeof resp.values === 'object') {
          Object.keys(resp.values).forEach((k) => {
            formData[k] = resp.values[k];
          });
        }
        // 构建字段列表
        if (Array.isArray(resp.fields) && resp.fields.length) {
          fields.value = resp.fields.map((f: any) => ({
            key: f.key || f.fieldKey,
            label: f.label || f.fieldName || f.key || f.fieldKey,
            type: f.type || 'input',
            perm: resp.permissions?.[f.key || f.fieldKey] || 'write',
            placeholder: f.placeholder || '',
            options: f.options || [],
          }));
        }
        return;
      }
      // 如果返回的是数组
      if (Array.isArray(data) && data.length) {
        fields.value = data.map((f: any) => ({
          key: f.key || f.fieldKey,
          label: f.label || f.fieldName || f.key || f.fieldKey,
          type: f.type || 'input',
          perm: f.permission || f.perm || 'write',
          placeholder: f.placeholder || '',
          options: f.options || [],
        }));
        data.forEach((f: any) => {
          const k = f.key || f.fieldKey;
          if (k && f.value !== undefined) formData[k] = f.value;
        });
      }
    } catch (err) {
      console.error('[form-engine] getFormFieldValueList failed', err);
    }
  }

  // ===== 模型切换 =====
  function onModelChange() {
    // 切换流程模型时重置状态
    flowStatus.value = '10';
    callbackRecords.value = [];
    lastCallbackResult.value = null;
    if (currentModel.value) {
      createMessage.info(`已切换到：${currentModel.value.processName}`);
    }
  }

  // ===== 角色切换 =====
  async function onRoleChange() {
    await loadFields();
    pushTrace('role_switch', '切换角色');
  }

  // ===== 操作禁用判断 =====
  function isOpDisabled(operation: string): boolean {
    // 废弃状态不可操作
    if (flowStatus.value === '00') return true;
    // 已通过状态不可操作
    if (flowStatus.value === '30') return true;
    // 转派/传阅仅审批中可用
    if (
      (operation === 'handler_assign' || operation === 'handler_communicate') &&
      flowStatus.value !== '20'
    ) {
      return true;
    }
    return false;
  }

  // ===== 审批操作处理 =====
  function handleOperation(operation: string) {
    const opInfo = BPM_OPERATION_TYPES.find((o) => o.value === operation);
    currentOpDesc.value = opInfo?.desc || '';
    switch (operation) {
      case 'handler_pass':
        handlePass();
        break;
      case 'handler_refuse':
        handleRefuse();
        break;
      case 'drafter_abandon':
        handleAbandon();
        break;
      case 'handler_assign':
        openAssignDialog('handler_assign');
        break;
      case 'handler_communicate':
        openAssignDialog('handler_communicate');
        break;
    }
  }

  // 审批通过：推进到下一节点
  function handlePass() {
    if (flowStatus.value === '10') {
      // 创建流程：10 -> 20（草稿 -> 审批中）
      flowStatus.value = '20';
      pushTrace('handler_pass', '创建流程（草稿->审批中）');
      createMessage.success('流程已创建，状态变更为审批中');
      loadFields();
      return;
    }
    if (flowStatus.value === '20') {
      // 审批通过推进节点
      if (currentNodeIndex.value >= nodes.value.length - 1) {
        // 最终节点：20 -> 30（审批中 -> 通过）
        flowStatus.value = '30';
        pushTrace('handler_pass', '审批通过（审批中->已通过）');
        createMessage.success('流程审批通过');
      } else {
        currentNodeIndex.value += 1;
        const node = nodes.value[currentNodeIndex.value];
        if (node?.handlerNames) currentRole.value = node.handlerNames;
        pushTrace('handler_pass', '审批通过，推进到下一节点');
        createMessage.success('已推进到下一节点');
        loadFields();
      }
    }
  }

  // 驳回：退回上一节点
  function handleRefuse() {
    if (currentNodeIndex.value <= 0) {
      createMessage.info('已是第一个节点，无法驳回');
      return;
    }
    currentNodeIndex.value -= 1;
    const node = nodes.value[currentNodeIndex.value];
    if (node?.handlerNames) currentRole.value = node.handlerNames;
    pushTrace('handler_refuse', '驳回，退回上一节点');
    createMessage.warning('已驳回到上一节点');
    loadFields();
  }

  // 废弃：状态变更为 00
  function handleAbandon() {
    flowStatus.value = '00';
    pushTrace('drafter_abandon', '废弃流程');
    createMessage.error('流程已废弃');
  }

  // 打开转派/传阅弹窗
  function openAssignDialog(operation: 'handler_assign' | 'handler_communicate') {
    assignType.value = operation;
    resetAssignForm();
    openAssignModal(true);
  }

  // 转派/传阅提交
  async function handleAssignSubmit() {
    try {
      const values = await validateAssignForm();
      const opLabel = assignType.value === 'handler_assign' ? '转派' : '传阅';
      pushTrace(assignType.value, `${opLabel}给：${values.targetUser}`);
      createMessage.success(`${opLabel}成功，目标：${values.targetUser}`);
      closeAssignModal();
    } catch {
      // 校验失败
    }
  }

  // ===== 执行回调 =====
  async function executeCallback(cb: { functionId: string; functionName: string }) {
    callbackLoading.value = cb.functionId;
    try {
      const result: any = await doMethodProcess({
        methodKey: cb.functionId,
        params: { ...formData, modelId: currentModelId.value },
      });
      const success = result?.success !== false;
      lastCallbackResult.value = result;
      // 回调结果回写表单字段值
      if (success && result?.data) {
        Object.keys(result.data).forEach((k) => {
          formData[k] = result.data[k];
        });
      }
      // 记录执行
      callbackRecords.value.unshift({
        time: nowTime(),
        functionId: cb.functionId,
        success,
      });
      pushTrace('callback', `执行回调：${cb.functionName}`);
      openResultModal(true);
    } catch (err) {
      console.error('[form-engine] doMethodProcess failed', err);
      lastCallbackResult.value = { success: false, message: '回调执行异常' };
      callbackRecords.value.unshift({
        time: nowTime(),
        functionId: cb.functionId,
        success: false,
      });
      openResultModal(true);
    } finally {
      callbackLoading.value = '';
    }
  }

  // ===== 轨迹回放：点击某条轨迹恢复到该步骤状态 =====
  function replayStep(index: number) {
    const item = trace.value[index];
    if (!item) return;
    replayIndex.value = index;
    // 恢复表单数据到该步骤快照
    Object.keys(formData).forEach((k) => delete formData[k]);
    Object.keys(item.formDataSnapshot).forEach((k) => {
      formData[k] = item.formDataSnapshot[k];
    });
    // 恢复节点
    const nodeIdx = nodes.value.findIndex((n) => n.nodeId === item.nodeId);
    if (nodeIdx >= 0) currentNodeIndex.value = nodeIdx;
    createMessage.info(`已回放到步骤 #${item.step}`);
  }

  // ===== 清空轨迹 =====
  function clearTrace() {
    trace.value = [];
    stepCounter.value = 0;
    replayIndex.value = -1;
  }

  // ===== 返回列表 =====
  function handleBack() {
    router.push('/form-engine/list');
  }

  // ===== 初始化 =====
  onMounted(async () => {
    await loadProcess();
    await loadFields();
    pushTrace('init', '流程启动');
  });
  // update-end---author:formengine ---date:2026-07-08  for：【流程仿真】对齐蓝凌BPM业务实际流程的仿真模拟---
</script>

<style scoped>
  .process-simulation {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f0f2f5;
  }

  .sim-header {
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
  .header-center {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .ctx-label {
    font-size: 13px;
    color: #666;
  }
  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .sim-body {
    flex: 1;
    display: flex;
    overflow: hidden;
  }

  /* update-begin---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图区域样式--- */
  .sim-flowchart {
    background: #fff;
    border-top: 1px solid #e8e8e8;
    border-bottom: 1px solid #e8e8e8;

    .flowchart-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 16px;
      cursor: pointer;
      user-select: none;
      background: #fafafa;

      .flowchart-title {
        font-weight: 600;
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 6px;
      }
      .flowchart-current {
        margin-left: 16px;
        font-size: 13px;
        color: #666;
      }
      .anticon-down {
        margin-left: auto;
        transition: transform 0.3s;
        &.collapsed {
          transform: rotate(-90deg);
        }
      }
    }

    .flowchart-body {
      padding: 8px;
    }
  }
  /* update-end---author:formengine ---date:2026-07-08  for：【流程仿真】BPMN流程图区域样式--- */

  .sim-left {
    flex: 1;
    background: #fff;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .panel-header {
    height: 44px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    font-weight: 600;
    font-size: 14px;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;
  }
  .role-switcher {
    padding: 10px 16px;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
  .role-label {
    font-size: 13px;
    color: #666;
  }
  .role-empty {
    font-size: 12px;
    color: #ccc;
  }
  .form-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    max-width: 600px;
  }

  .sim-right {
    width: 360px;
    background: #fff;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    overflow: hidden;
    gap: 8px;
    padding: 8px;
  }
  .ops-card {
    flex-shrink: 0;
  }
  .ops-trace {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .ops-trace :deep(.ant-card-body) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .status-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
    font-size: 13px;
  }
  .row-label {
    color: #666;
    min-width: 70px;
  }
  .text-muted {
    color: #ccc;
    font-size: 12px;
  }
  .op-btn-group {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
  .op-btn {
    flex: 1 1 calc(50% - 3px);
    min-width: 80px;
  }
  .op-desc {
    margin-top: 6px;
    min-height: 16px;
    font-size: 11px;
  }

  .callback-list {
    max-height: 160px;
    overflow-y: auto;
  }
  .callback-records {
    margin-top: 8px;
    border-top: 1px solid #f0f0f0;
    padding-top: 8px;
  }
  .record-title {
    font-size: 12px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  .record-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11px;
    margin-bottom: 2px;
  }
  .record-time {
    color: #ccc;
    font-variant-numeric: tabular-nums;
  }
  .record-fn {
    color: #333;
    flex: 1;
    font-family: monospace;
  }

  .trace-list {
    flex: 1;
    overflow-y: auto;
  }
  .trace-empty {
    text-align: center;
    color: #999;
    font-size: 12px;
    padding: 20px 0;
  }
  .trace-item {
    padding: 8px 10px;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    margin-bottom: 6px;
    cursor: pointer;
    transition: all 0.2s;
  }
  .trace-item:hover {
    border-color: #91caff;
    background: #f0f7ff;
  }
  .trace-item.replay-active {
    border-color: #4096ff;
    background: #e6f4ff;
    box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2);
  }
  .trace-head {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11px;
    margin-bottom: 4px;
  }
  .trace-step {
    color: #4096ff;
    font-weight: 700;
  }
  .trace-node {
    color: #666;
    flex: 1;
  }
  .trace-time {
    color: #ccc;
    font-variant-numeric: tabular-nums;
  }
  .trace-action {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
  }
  .trace-snapshot {
    color: #999;
    font-size: 10px;
    font-family: monospace;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 180px;
  }

  .sim-footer {
    background: #fff;
    border-top: 1px solid #e8e8e8;
    flex-shrink: 0;
    max-height: 280px;
    overflow: hidden;
    display: flex;
  }
  .footer-section {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border-right: 1px solid #f0f0f0;
  }
  .footer-section:last-child {
    border-right: none;
  }
  .footer-header {
    padding: 6px 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 13px;
    font-weight: 600;
    border-bottom: 1px solid #f0f0f0;
    flex-shrink: 0;
  }
  .footer-header .anticon {
    transition: transform 0.2s;
  }
  .footer-header .anticon.collapsed {
    transform: rotate(-90deg);
  }
  .json-preview {
    margin: 0;
    padding: 10px 16px;
    background: #1e1e2e;
    color: #e5e7eb;
    font-family: 'SF Mono', Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 1.6;
    overflow: auto;
    flex: 1;
    max-height: 220px;
  }

  .result-preview {
    background: #1e1e2e;
    color: #e5e7eb;
    padding: 16px;
    border-radius: 6px;
    font-family: 'SF Mono', Menlo, Consolas, monospace;
    font-size: 13px;
    line-height: 1.6;
    overflow: auto;
    max-height: calc(100vh - 200px);
    margin: 0;
  }
</style>
