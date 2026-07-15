<template>
  <div class="form-designer">
    <!-- 顶部工具栏 -->
    <div class="designer-header">
      <div class="header-left">
        <span class="header-title">表单设计器</span>
        <a-tag v-if="processId" color="blue">流程：{{ processId }}</a-tag>
        <a-tag v-if="templateId" color="cyan">模板：{{ templateId }}</a-tag>
        <!-- 全局配置：当前节点 + 当前角色 -->
        <a-divider type="vertical" />
        <span class="global-config-label">当前节点：</span>
        <a-select
          v-model:value="activeNodeId"
          size="small"
          style="width: 160px"
          placeholder="选择节点"
          allow-clear
          @change="onActiveNodeChange"
        >
          <a-select-option v-for="node in processNodes" :key="node.nodeId" :value="node.nodeId">
            {{ node.name }}（{{ node.nodeId }}）
          </a-select-option>
        </a-select>
        <span class="global-config-label">当前角色：</span>
        <a-select
          v-model:value="activeRoleValue"
          size="small"
          style="width: 120px"
          placeholder="选择角色"
          allow-clear
          @change="onActiveRoleChange"
        >
          <a-select-option v-for="role in SIMULATION_ROLES" :key="role.value" :value="role.value">
            <a-tag :color="role.color" size="small">{{ role.label }}</a-tag>
          </a-select-option>
        </a-select>
      </div>
      <div class="header-right">
        <a-button type="primary" :loading="saving" @click="handleSave">
          <template #icon><SaveOutlined /></template>
          保存
        </a-button>
        <a-button @click="handleOpenPreview">
          <template #icon><EyeOutlined /></template>
          预览
        </a-button>
        <a-button @click="handleOpenImport">
          <template #icon><ImportOutlined /></template>
          导入JSON
        </a-button>
        <a-button @click="handleOpenExport">
          <template #icon><ExportOutlined /></template>
          导出JSON
        </a-button>
        <a-button @click="handleBack">
          <template #icon><RollbackOutlined /></template>
          返回列表
        </a-button>
      </div>
    </div>

    <!-- 三栏工作区 -->
    <div class="designer-body">
      <!-- 左侧：组件面板 -->
      <div class="designer-left">
        <div class="panel-header">组件库</div>
        <div class="component-list">
          <div v-for="group in groupedComponents" :key="group.name" class="component-group">
            <div class="group-title" @click="toggleGroup(group.name)">
              <span>{{ group.name }}</span>
              <DownOutlined :class="{ 'group-arrow': true, collapsed: collapsedGroups[group.name] }" />
            </div>
            <div v-show="!collapsedGroups[group.name]" class="group-items">
              <div
                v-for="comp in group.items"
                :key="comp.type"
                class="component-item"
                draggable="true"
                @click="addComponent(comp.type)"
                @dragstart="handleDragStart($event, comp.type)"
              >
                <component :is="comp.icon" class="comp-icon" />
                <span class="comp-label">{{ comp.label }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间：画布 -->
      <div class="designer-center">
        <div class="panel-header">
          <span>画布（共 {{ schema.length }} 个组件）</span>
          <div class="canvas-header-right">
            <!-- 全局状态提示：当前激活的节点+角色 -->
            <a-tag v-if="activeNodeId" color="blue" size="small">
              节点：{{ getActiveNodeName() }}
            </a-tag>
            <a-tag v-if="activeRoleValue" :color="getActiveRoleColor()" size="small">
              角色：{{ getActiveRoleLabel() }}
            </a-tag>
            <a-button size="small" type="text" @click="handleClearCanvas" :disabled="schema.length === 0">清空</a-button>
          </div>
        </div>
        <div
          class="canvas-area"
          @dragover.prevent
          @drop="handleDrop"
          :class="{ 'drag-over': isDragOver }"
          @dragenter="isDragOver = true"
          @dragleave="isDragOver = false"
        >
          <a-empty v-if="schema.length === 0" description="点击或拖拽左侧组件添加到画布" />
          <div v-else class="canvas-list">
            <div
              v-for="(item, index) in schema"
              :key="item._id"
              class="canvas-item"
              :class="{
                selected: item._id === selectedId,
                'state-hidden': getFieldDisplayState(item) === 'hidden',
                'state-readonly': getFieldDisplayState(item) === 'readonly',
                'state-editable': getFieldDisplayState(item) === 'editable',
              }"
              @click="selectComponent(item._id)"
            >
              <div class="canvas-item-header">
                <span class="canvas-item-index">{{ index + 1 }}</span>
                <a-tag :color="getCompColor(item.type)">{{ getTypeLabel(item.type) }}</a-tag>
                <span class="item-title">{{ item.title || '未命名' }}</span>
                <a-tag v-if="item.field" size="small" color="geekblue">{{ item.field }}</a-tag>
                <!-- 动态展示状态标记（根据当前节点+角色） -->
                <a-tag
                  v-if="activeNodeId || activeRoleValue"
                  :color="getDisplayStateColor(getFieldDisplayState(item))"
                  size="small"
                >
                  {{ getDisplayStateLabel(getFieldDisplayState(item)) }}
                </a-tag>
                <span class="canvas-item-spacer"></span>
                <div class="canvas-item-actions" @click.stop>
                  <a-tooltip title="复制">
                    <a-button size="small" type="text" @click="copyComponent(item._id)">
                      <CopyOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="上移">
                    <a-button size="small" type="text" :disabled="index === 0" @click="moveUp(index)">
                      <UpOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="下移">
                    <a-button size="small" type="text" :disabled="index === schema.length - 1" @click="moveDown(index)">
                      <DownOutlined />
                    </a-button>
                  </a-tooltip>
                  <a-tooltip title="删除">
                    <a-button size="small" type="text" danger @click="deleteComponent(item._id)">
                      <DeleteOutlined />
                    </a-button>
                  </a-tooltip>
                </div>
              </div>
              <div class="canvas-item-preview">
                <!-- 预览渲染：使用模板语法替代 h() 渲染函数，更稳定 -->
                <PreviewRenderer :item="item" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：属性配置（Tab切换：基础属性 / 流程节点状态） -->
      <div class="designer-right">
        <div class="panel-header">
          <span>属性配置</span>
        </div>
        <a-empty v-if="!selectedComp" description="请选择一个组件" style="margin-top: 80px" />
        <div v-else class="props-tab-container">
          <!-- update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】右侧属性区改为Tab切换形式---
               需求2:基础属性(Schema回填)和流程节点状态(auth)调整为Tab切换形式--- -->
          <a-tabs v-model:activeKey="rightPanelActiveTab" size="small" class="props-tabs">
            <!-- Tab1: 基础属性 + 组件属性 -->
            <a-tab-pane key="props" tab="基础属性">
              <a-form layout="vertical" size="small">
                <!-- 基础属性（只读回填，从Schema驱动） -->
                <a-divider orientation="left" plain>基础属性（Schema回填）</a-divider>
                <a-alert
                  v-if="formSchemaFields.length === 0"
                  message="尚未配置流程表单Schema，请先在「流程列表」点击「Schema」按钮配置字段"
                  type="warning"
                  show-icon
                  style="margin-bottom: 12px; font-size: 12px"
                />
                <a-form-item label="绑定Schema字段">
                  <a-select
                    v-model:value="selectedComp.field"
                    show-search
                    allow-clear
                    placeholder="选择Schema字段绑定"
                    :options="schemaFieldOptions"
                    @change="onSchemaFieldBind"
                  />
                </a-form-item>
                <a-form-item label="显示名称（title）">
                  <a-input :value="selectedComp.title" disabled placeholder="选择Schema字段后自动回填" />
                </a-form-item>
                <a-form-item label="组件类型（type）">
                  <a-input :value="getTypeLabel(selectedComp.type)" disabled />
                </a-form-item>
                <a-form-item label="字段类型（fieldType）">
                  <a-input :value="getBoundSchemaField(selectedComp.field)?.fieldType || '-'" disabled />
                </a-form-item>
                <a-row :gutter="8">
                  <a-col :span="12">
                    <a-form-item label="必填">
                      <a-switch :checked="isRequiredFromSchema(selectedComp.field)" disabled />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="默认值">
                      <a-input :value="getBoundSchemaField(selectedComp.field)?.defaultValue || ''" disabled />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item v-if="getBoundSchemaField(selectedComp.field)?.pattern" label="正则校验">
                  <a-input :value="getBoundSchemaField(selectedComp.field)?.pattern" disabled />
                </a-form-item>
                <a-row
                  v-if="getBoundSchemaField(selectedComp.field)?.min !== undefined || getBoundSchemaField(selectedComp.field)?.max !== undefined"
                  :gutter="8"
                >
                  <a-col :span="12">
                    <a-form-item label="最小值/最小长度">
                      <a-input-number
                        :value="getBoundSchemaField(selectedComp.field)?.min"
                        disabled
                        style="width: 100%"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="最大值/最大长度">
                      <a-input-number
                        :value="getBoundSchemaField(selectedComp.field)?.max"
                        disabled
                        style="width: 100%"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>

                <!-- 组件属性（可编辑） -->
                <a-divider orientation="left" plain>组件属性（props）</a-divider>
                <a-form-item v-if="hasPlaceholder" label="占位提示">
                  <a-input v-model:value="selectedComp.props.placeholder" placeholder="placeholder" />
                </a-form-item>
                <template v-if="['select', 'radio', 'checkbox'].includes(selectedComp.type)">
                  <a-form-item label="选项（每行一个，label:value）">
                    <a-textarea
                      v-model:value="optionsText"
                      :rows="4"
                      placeholder="选项一:1&#10;选项二:2"
                    />
                  </a-form-item>
                </template>
                <template v-if="selectedComp.type === 'number'">
                  <a-row :gutter="8">
                    <a-col :span="12">
                      <a-form-item label="最小值">
                        <a-input-number v-model:value="selectedComp.props.min" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="最大值">
                        <a-input-number v-model:value="selectedComp.props.max" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                </template>
                <template v-if="selectedComp.type === 'textarea'">
                  <a-form-item label="行数">
                    <a-input-number v-model:value="selectedComp.props.rows" :min="1" :max="20" style="width: 100%" />
                  </a-form-item>
                </template>
                <template v-if="selectedComp.type === 'button'">
                  <a-form-item label="绑定业务方法">
                    <a-select
                      v-model:value="selectedComp.props.methodKey"
                      show-search
                      allow-clear
                      placeholder="选择业务方法"
                      :options="methodOptions"
                    />
                  </a-form-item>
                </template>
              </a-form>
            </a-tab-pane>

            <!-- Tab2: 流程节点状态 + 角色状态 -->
            <a-tab-pane key="state" tab="流程节点状态">
              <a-form layout="vertical" size="small">
                <!-- 流程节点状态配置 -->
                <a-divider orientation="left" plain>流程节点状态（auth）</a-divider>
                <a-alert
                  v-if="processNodes.length === 0"
                  message="尚未导入流程节点，请点击顶部「导入XML」按钮导入 process.xml 文件"
                  type="warning"
                  show-icon
                  style="margin-bottom: 12px; font-size: 12px"
                />
                <template v-else>
                  <a-form-item label="配置节点">
                    <a-select
                      v-model:value="configNodeId"
                      placeholder="选择要配置状态的节点"
                      style="width: 100%"
                    >
                      <a-select-option v-for="node in processNodes" :key="node.nodeId" :value="node.nodeId">
                        {{ node.name }}（{{ node.nodeId }}）
                      </a-select-option>
                    </a-select>
                  </a-form-item>
                  <!-- update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】移除「当前节点字段状态」表单项--- -->
                  <!-- 已删除：当前节点字段状态的a-radio-group表单项 -->
                  <!-- update-end---author:formengine ---date:2026-07-15  for：【表单设计器】移除「当前节点字段状态」表单项--- -->
                  <table v-if="configNodeId" class="perm-table">
                    <thead>
                      <tr>
                        <th>节点（ID）</th>
                        <th v-for="at in AUTH_TYPES" :key="at.value">{{ at.label }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="node in processNodes" :key="node.nodeId">
                        <td class="role-cell">
                          <div class="node-cell">
                            <a-tag :color="node.nodeId === configNodeId ? 'blue' : 'default'">{{ node.name }}</a-tag>
                            <span class="node-id-text">{{ node.nodeId }}</span>
                          </div>
                        </td>
                        <td v-for="at in AUTH_TYPES" :key="at.value">
                          <a-radio
                            :checked="getFieldAuth(selectedComp, node.nodeId) === at.value"
                            @change="setFieldAuth(selectedComp, node.nodeId, at.value)"
                          />
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </template>

                <!-- 角色状态配置矩阵 -->
                <a-divider orientation="left" plain>角色状态配置（permissions）</a-divider>
                <table class="perm-table">
                  <thead>
                    <tr>
                      <th>角色</th>
                      <th v-for="pt in PERMISSION_TYPES" :key="pt.value">{{ pt.label }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="role in SIMULATION_ROLES" :key="role.value">
                      <td class="role-cell">
                        <a-tag :color="role.color">{{ role.label }}</a-tag>
                      </td>
                      <td v-for="pt in PERMISSION_TYPES" :key="pt.value">
                        <a-radio
                          :checked="selectedComp.permissions[role.value] === pt.value"
                          @change="selectedComp.permissions[role.value] = pt.value"
                        />
                      </td>
                    </tr>
                  </tbody>
                </table>
              </a-form>
            </a-tab-pane>
          </a-tabs>
          <!-- update-end---author:formengine ---date:2026-07-15  for：【表单设计器】右侧属性区改为Tab切换形式--- -->
        </div>
      </div>
    </div>

    <!-- 导入JSON全屏弹窗 -->
    <BasicModal
      @register="registerImportModal"
      title="导入JSON"
      :defaultFullscreen="true"
      okText="导入"
      @ok="handleImportSubmit"
    >
      <a-alert message="粘贴 form-create JSON 数组，导入后将替换当前画布内容" type="info" show-icon style="margin-bottom: 16px" />
      <a-textarea
        v-model:value="importJsonText"
        :rows="24"
        placeholder='[{"type":"input","field":"name","title":"姓名","props":{"placeholder":"请输入"},"validate":[],"permissions":{"applicant":"write","manager":"readonly","finance":"readonly"}}]'
        style="font-family: monospace; font-size: 13px"
      />
    </BasicModal>

    <!-- 导出JSON全屏弹窗 -->
    <BasicModal
      @register="registerExportModal"
      title="导出JSON"
      :defaultFullscreen="true"
      :showOkBtn="false"
      cancelText="关闭"
    >
      <div style="margin-bottom: 12px">
        <a-button type="primary" @click="handleCopyExport">
          <template #icon><CopyOutlined /></template>
          复制到剪贴板
        </a-button>
        <a-button style="margin-left: 8px" @click="handleDownloadExport">
          <template #icon><DownloadOutlined /></template>
          下载文件
        </a-button>
      </div>
      <pre class="json-preview">{{ exportJsonText }}</pre>
    </BasicModal>

    <!-- 表单预览全屏弹窗 -->
    <BasicModal
      @register="registerPreviewModal"
      title="表单预览"
      :defaultFullscreen="true"
      :showOkBtn="false"
      cancelText="关闭"
    >
      <div class="preview-container">
        <a-alert message="以下是表单运行时渲染效果预览" type="info" show-icon style="margin-bottom: 16px" />
        <a-form layout="vertical" style="max-width: 700px; margin: 0 auto">
          <template v-if="schema.length === 0">
            <a-empty description="画布为空，请先添加组件" />
          </template>
          <template v-else>
            <a-form-item
              v-for="item in schema"
              :key="item._id"
              :label="item.title"
              :required="item.validate && item.validate.some((r: any) => r.required)"
            >
              <PreviewRenderer :item="item" :editable="true" v-model:value="previewData[item.field]" />
            </a-form-item>
          </template>
        </a-form>
        <a-divider v-if="schema.length > 0" />
        <div v-if="schema.length > 0" class="preview-data">
          <div class="preview-data-title">当前表单数据：</div>
          <pre class="json-preview-small">{{ JSON.stringify(previewData, null, 2) }}</pre>
        </div>
      </div>
    </BasicModal>
  </div>
</template>

<script setup lang="ts">
  // update-begin---author:formengine ---date:2026-07-08  for：【表单设计器】全面完善：三栏骨架+拖拽+预览+复制+导出，模板语法替代h()---
  import { computed, onMounted, reactive, ref, defineComponent, h } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import {
    SaveOutlined,
    ImportOutlined,
    ExportOutlined,
    RollbackOutlined,
    UpOutlined,
    DownOutlined,
    DeleteOutlined,
    CopyOutlined,
    EyeOutlined,
    DownloadOutlined,
  } from '@ant-design/icons-vue';
  import {
    Input,
    InputNumber,
    Textarea,
    Select,
    Radio,
    Checkbox,
    DatePicker,
    Switch,
    Button,
    Divider,
    Alert,
    Typography,
    Empty,
  } from 'ant-design-vue';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    editProcess,
    getProcessById,
    getFormFieldList,
    getMethodInfo,
    initDesignerLayout,
    getSchemaFields,
  } from '/@/api/form-engine';
  import { COMPONENT_TYPES, SIMULATION_ROLES, PERMISSION_TYPES, AUTH_TYPES, FIELD_TYPE_TO_COMPONENT, type NodeFormField } from './FormEngine.data';

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();

  const processId = computed(() => (route.query.processId as string) || '');
  const templateId = computed(() => (route.query.templateId as string) || '');

  // update-begin---author:formengine ---date:2026-07-08  for：【表单设计器】流程节点字段权限控制（auth=0可编辑/1不可见/2只读）---
  // ===== form-create 组件规则数据结构 =====
  interface FormRule {
    _id: string;
    type: string;
    field: string;
    title: string;
    props: Record<string, any>;
    validate: any[];
    permissions: { applicant: string; manager: string; finance: string };
    /** 节点字段权限：{ [nodeId]: authValue }，auth: 0=可编辑, 1=不可见, 2=只读 */
    nodePermissions?: Record<string, number>;
  }

  /** 流程节点结构 */
  interface ProcessNode {
    nodeId: string;
    name: string;
    role?: string;
    formTemplateId?: string;
    /** 节点类型（蓝凌BPM: startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode） */
    nodeType?: string;
  }

  // ===== 画布数据 =====
  const schema = reactive<FormRule[]>([]);
  const selectedId = ref<string>('');
  const saving = ref(false);
  const compSeq = ref(0);
  const isDragOver = ref(false);
  const draggedType = ref<string>('');
  const collapsedGroups = reactive<Record<string, boolean>>({});
  const previewData = reactive<Record<string, any>>({});

  // ===== 流程节点状态 =====
  /** 从流程 processDef（XML）解析得到的流程节点列表 */
  const processNodes = ref<ProcessNode[]>([]);
  /** 右侧属性面板中正在配置状态的节点ID */
  const configNodeId = ref<string>('');
  // update-end---author:formengine ---date:2026-07-08  for：【表单设计器】流程节点字段权限控制（auth=0可编辑/1不可见/2只读）---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】表单Schema改为流程级加载（所有节点共享）---
  /** 流程级表单Schema字段列表（从流程记录的 formSchemaFields 加载，所有节点共享） */
  const formSchemaFields = ref<NodeFormField[]>([]);
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】表单Schema改为流程级加载（所有节点共享）---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】移除前置检查逻辑，改为Tab切换状态---
  /** 右侧属性面板当前激活的Tab（props=基础属性, state=流程节点状态） */
  const rightPanelActiveTab = ref<'props' | 'state'>('props');
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】移除前置检查逻辑，改为Tab切换状态---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】全局配置：当前节点 + 当前角色，影响画布动态展示---
  /** 全局：当前激活的节点ID（影响画布展示状态） */
  const activeNodeId = ref<string>('');
  /** 全局：当前激活的角色值（影响画布展示状态） */
  const activeRoleValue = ref<string>('');

  /** 全局节点切换回调 */
  function onActiveNodeChange() {
    // 切换节点时，画布根据 activeNodeId + activeRoleValue 动态展示
  }

  /** 全局角色切换回调 */
  function onActiveRoleChange() {
    // 切换角色时，画布根据 activeNodeId + activeRoleValue 动态展示
  }

  /** 获取当前激活节点的名称 */
  function getActiveNodeName(): string {
    const node = processNodes.value.find((n) => n.nodeId === activeNodeId.value);
    return node?.name || activeNodeId.value || '';
  }

  /** 获取当前激活角色的标签 */
  function getActiveRoleLabel(): string {
    const role = SIMULATION_ROLES.find((r) => r.value === activeRoleValue.value);
    return role?.label || activeRoleValue.value || '';
  }

  /** 获取当前激活角色的颜色 */
  function getActiveRoleColor(): string {
    const role = SIMULATION_ROLES.find((r) => r.value === activeRoleValue.value);
    return role?.color || 'default';
  }

  /**
   * 计算表单元素在当前激活节点+角色下的展示状态
   * @returns 'editable' | 'readonly' | 'hidden' | 'normal'
   *   - hidden: 不可见（节点auth=1 或 角色hidden）
   *   - readonly: 只读（节点auth=2 或 角色readonly）
   *   - editable/normal: 可编辑/正常
   */
  function getFieldDisplayState(item: any): 'editable' | 'readonly' | 'hidden' | 'normal' {
    // 未激活节点和角色时，正常展示
    if (!activeNodeId.value && !activeRoleValue.value) return 'normal';
    // 节点权限优先判断
    if (activeNodeId.value) {
      const auth = getFieldAuth(item, activeNodeId.value);
      if (auth === 1) return 'hidden';
      if (auth === 2) return 'readonly';
    }
    // 角色权限判断
    if (activeRoleValue.value && item.permissions) {
      const perm = item.permissions[activeRoleValue.value];
      if (perm === 'hidden') return 'hidden';
      if (perm === 'readonly') return 'readonly';
    }
    return activeNodeId.value || activeRoleValue.value ? 'editable' : 'normal';
  }

  /** 展示状态 -> 标签文字 */
  function getDisplayStateLabel(state: string): string {
    const map: Record<string, string> = {
      editable: '可编辑',
      readonly: '只读',
      hidden: '隐藏',
      normal: '正常',
    };
    return map[state] || state;
  }

  /** 展示状态 -> 标签颜色 */
  function getDisplayStateColor(state: string): string {
    const map: Record<string, string> = {
      editable: 'green',
      readonly: 'blue',
      hidden: 'red',
      normal: 'default',
    };
    return map[state] || 'default';
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】全局配置：当前节点 + 当前角色，影响画布动态展示---

  // ===== 字段列表 & 方法列表 =====
  const fieldList = ref<any[]>([]);
  const methodList = ref<any[]>([]);

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】Schema字段绑定与基础属性只读回填---
  /** Schema字段选项（用于基础属性中的字段绑定下拉） */
  const schemaFieldOptions = computed(() => {
    return formSchemaFields.value
      .filter((f) => f.fieldKey)
      .map((f) => ({
        label: `${f.fieldKey} (${f.fieldLabel})`,
        value: f.fieldKey,
      }));
  });

  /** 根据fieldKey获取已绑定的Schema字段定义 */
  function getBoundSchemaField(fieldKey: string): NodeFormField | undefined {
    if (!fieldKey) return undefined;
    return formSchemaFields.value.find((f) => f.fieldKey === fieldKey);
  }

  /** 从Schema获取必填属性（只读回填） */
  function isRequiredFromSchema(fieldKey: string): boolean {
    const field = getBoundSchemaField(fieldKey);
    return field?.required || false;
  }

  /**
   * Schema字段绑定变更回调
   * 当用户选择Schema字段后，自动回填基础属性（title/type/validate等），均只读
   */
  function onSchemaFieldBind(fieldKey: string) {
    if (!selectedComp.value) return;
    const field = getBoundSchemaField(fieldKey);
    if (!field) {
      // 清空绑定
      selectedComp.value.title = '';
      selectedComp.value.validate = [];
      return;
    }
    // 1. 回填标题
    selectedComp.value.title = field.fieldLabel || field.fieldKey;
    // 2. 根据Schema字段类型自动映射组件类型
    const allowedTypes = FIELD_TYPE_TO_COMPONENT[field.fieldType] || ['input'];
    if (!allowedTypes.includes(selectedComp.value.type)) {
      // 当前组件类型不在允许列表中，自动切换为第一个允许的类型
      selectedComp.value.type = allowedTypes[0];
    }
    // 3. 回填验证规则（必填、正则、min/max）
    const validate: any[] = [];
    if (field.required) {
      validate.push({ required: true, message: `${field.fieldLabel || field.fieldKey}为必填项`, trigger: 'blur' });
    }
    if (field.pattern) {
      validate.push({ pattern: new RegExp(field.pattern), message: field.validationMessage || '格式不正确', trigger: 'blur' });
    }
    if (field.min !== undefined && field.min !== null) {
      if (field.fieldType === 'number') {
        validate.push({ type: 'number', min: field.min, message: `不能小于${field.min}`, trigger: 'blur' });
      } else {
        validate.push({ min: field.min, message: `长度不能小于${field.min}`, trigger: 'blur' });
      }
    }
    if (field.max !== undefined && field.max !== null) {
      if (field.fieldType === 'number') {
        validate.push({ type: 'number', max: field.max, message: `不能大于${field.max}`, trigger: 'blur' });
      } else {
        validate.push({ max: field.max, message: `长度不能大于${field.max}`, trigger: 'blur' });
      }
    }
    selectedComp.value.validate = validate;
    // 4. 回填默认值到 props
    if (field.defaultValue !== undefined && field.defaultValue !== '') {
      selectedComp.value.props.defaultValue = field.defaultValue;
    }
    createMessage.success(`已绑定Schema字段：${field.fieldLabel || field.fieldKey}`);
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】Schema字段绑定与基础属性只读回填---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】移除旧fieldOptions（已被schemaFieldOptions替代）---
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】移除旧fieldOptions（已被schemaFieldOptions替代）---
  const methodOptions = computed(() =>
    methodList.value.map((m) => ({
      label: `${m.methodKey || m.key} (${m.methodName || m.name || ''})`,
      value: m.methodKey || m.key,
    })),
  );

  // ===== 组件分组 =====
  const groupedComponents = computed(() => {
    const map: Record<string, any[]> = {};
    COMPONENT_TYPES.forEach((c) => {
      if (!map[c.group]) map[c.group] = [];
      map[c.group].push(c);
    });
    return Object.keys(map).map((name) => ({ name, items: map[name] }));
  });

  function toggleGroup(name: string) {
    collapsedGroups[name] = !collapsedGroups[name];
  }

  // ===== 选中组件 =====
  const selectedComp = computed(() => schema.find((c) => c._id === selectedId.value) || null);

  // ===== 是否有 placeholder 属性 =====
  const hasPlaceholder = computed(() => {
    if (!selectedComp.value) return false;
    return ['input', 'textarea', 'number', 'select', 'date'].includes(selectedComp.value.type);
  });

  // ===== 选项文本双向绑定 =====
  const optionsText = computed({
    get: () => {
      const opts = selectedComp.value?.props?.options;
      if (!opts || !Array.isArray(opts)) return '';
      return opts.map((o: any) => `${o.label}:${o.value}`).join('\n');
    },
    set: (val: string) => {
      if (!selectedComp.value) return;
      const lines = val.split('\n').filter((l) => l.trim());
      selectedComp.value.props.options = lines.map((line) => {
        const [label, value] = line.split(':');
        return { label: (label || '').trim(), value: (value || label || '').trim() };
      });
    },
  });

  // ===== 预览渲染组件（使用模板+render混合，覆盖所有组件类型） =====
  const PreviewRenderer = defineComponent({
    name: 'PreviewRenderer',
    props: {
      item: { type: Object, required: true },
      editable: { type: Boolean, default: false },
      value: { type: [String, Number, Boolean, Array, Object], default: undefined },
    },
    emits: ['update:value'],
    setup(props, { emit }) {
      return () => {
        const item = props.item as any;
        const editable = props.editable;
        const disabled = !editable;
        const placeholder = item.props?.placeholder || '';
        const val = props.value;

        const updateVal = (v: any) => emit('update:value', v);

        switch (item.type) {
          case 'input':
            return h(Input, {
              placeholder,
              disabled,
              value: val,
              'onUpdate:value': updateVal,
              size: 'small',
            });
          case 'textarea':
            return h(Textarea, {
              placeholder,
              disabled,
              value: val,
              'onUpdate:value': updateVal,
              rows: item.props?.rows || 2,
              size: 'small',
            });
          case 'number':
            return h(InputNumber, {
              placeholder,
              disabled,
              value: val,
              'onUpdate:value': updateVal,
              size: 'small',
              style: 'width:100%',
              min: item.props?.min,
              max: item.props?.max,
            });
          case 'select':
            return h(Select, {
              placeholder,
              disabled,
              value: val,
              'onUpdate:value': updateVal,
              size: 'small',
              style: 'width:100%',
              options: item.props?.options || [],
            });
          case 'radio':
            return h(
              Radio.Group,
              { disabled, value: val, 'onUpdate:value': updateVal, size: 'small' },
              () => (item.props?.options || []).map((o: any) => h(Radio, { value: o.value }, () => o.label)),
            );
          case 'checkbox':
            return h(
              Checkbox.Group,
              { disabled, value: val, 'onUpdate:value': updateVal },
              () => (item.props?.options || []).map((o: any) => h(Checkbox, { value: o.value }, () => o.label)),
            );
          case 'date':
            return h(DatePicker, {
              placeholder,
              disabled,
              value: val,
              'onUpdate:value': updateVal,
              size: 'small',
              style: 'width:100%',
            });
          case 'switch':
            return h(Switch, {
              disabled,
              checked: val,
              'onUpdate:checked': updateVal,
              size: 'small',
            });
          case 'button':
            return h(
              Button,
              { type: 'primary', size: 'small', disabled },
              () => item.title || '按钮',
            );
          case 'title':
            return h(Typography.Title, { level: 5 }, () => item.title || '标题');
          case 'divider':
            return h(Divider, { style: 'margin: 4px 0' });
          case 'alert':
            return h(Alert, { message: item.title || '提示信息', type: 'info', showIcon: true });
          default:
            return h('span', { style: 'color:#999' }, `未知组件: ${item.type}`);
        }
      };
    },
  });

  // ===== 工具函数 =====
  function getCompColor(type: string) {
    const colors: Record<string, string> = {
      input: 'blue',
      textarea: 'blue',
      number: 'blue',
      select: 'cyan',
      radio: 'cyan',
      checkbox: 'cyan',
      date: 'geekblue',
      switch: 'geekblue',
      button: 'orange',
      title: 'purple',
      divider: 'purple',
      alert: 'purple',
    };
    return colors[type] || 'default';
  }

  function getTypeLabel(type: string) {
    const found = COMPONENT_TYPES.find((c) => c.type === type);
    return found ? found.label : type;
  }

  function nextId() {
    compSeq.value += 1;
    return `rule_${compSeq.value}`;
  }

  function createRule(type: string): FormRule {
    const defaultRule: FormRule = {
      _id: nextId(),
      type,
      field: '',
      title: getTypeLabel(type),
      props: {},
      validate: [],
      permissions: { applicant: 'write', manager: 'readonly', finance: 'readonly' },
      nodePermissions: {},
    };
    // 根据类型设置默认 props
    if (['input', 'textarea', 'number', 'select', 'date'].includes(type)) {
      defaultRule.props.placeholder = '请输入';
    }
    if (['select', 'radio', 'checkbox'].includes(type)) {
      defaultRule.props.options = [
        { label: '选项一', value: '1' },
        { label: '选项二', value: '2' },
      ];
    }
    if (type === 'number') {
      defaultRule.props.min = 0;
      defaultRule.props.max = 999999;
    }
    if (type === 'textarea') {
      defaultRule.props.rows = 3;
    }
    return defaultRule;
  }

  // ===== 组件操作 =====
  function addComponent(type: string) {
    const rule = createRule(type);
    schema.push(rule);
    selectedId.value = rule._id;
  }

  function copyComponent(id: string) {
    const idx = schema.findIndex((c) => c._id === id);
    if (idx < 0) return;
    const original = schema[idx];
    const copy: FormRule = {
      _id: nextId(),
      type: original.type,
      field: original.field ? original.field + '_copy' : '',
      title: original.title + ' (副本)',
      props: JSON.parse(JSON.stringify(original.props)),
      validate: JSON.parse(JSON.stringify(original.validate)),
      permissions: { ...original.permissions },
      nodePermissions: { ...(original.nodePermissions || {}) },
    };
    schema.splice(idx + 1, 0, copy);
    selectedId.value = copy._id;
    createMessage.success('已复制组件');
  }

  function deleteComponent(id: string) {
    const idx = schema.findIndex((c) => c._id === id);
    if (idx >= 0) schema.splice(idx, 1);
    if (selectedId.value === id) selectedId.value = '';
  }

  function selectComponent(id: string) {
    selectedId.value = id;
  }

  function moveUp(index: number) {
    if (index === 0) return;
    const tmp = schema[index];
    schema.splice(index, 1);
    schema.splice(index - 1, 0, tmp);
  }

  function moveDown(index: number) {
    if (index === schema.length - 1) return;
    const tmp = schema[index];
    schema.splice(index, 1);
    schema.splice(index + 1, 0, tmp);
  }

  function handleClearCanvas() {
    schema.splice(0, schema.length);
    selectedId.value = '';
  }

  // ===== 拖拽 =====
  function handleDragStart(e: DragEvent, type: string) {
    draggedType.value = type;
    if (e.dataTransfer) {
      e.dataTransfer.effectAllowed = 'copy';
    }
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault();
    isDragOver.value = false;
    if (draggedType.value) {
      addComponent(draggedType.value);
      draggedType.value = '';
    }
  }

  // update-begin---author:formengine ---date:2026-07-08  for：【表单设计器】流程节点字段权限控制核心逻辑---
  /**
   * 获取字段在指定节点的权限值（auth）
   * @param item 表单组件规则
   * @param nodeId 流程节点ID
   * @returns auth 值：0=可编辑, 1=不可见, 2=只读；未配置默认返回 0（可编辑）
   */
  function getFieldAuth(item: any, nodeId?: string): number {
    if (!nodeId || !item) return 0;
    const perms = item.nodePermissions;
    if (!perms || typeof perms !== 'object') return 0;
    const val = perms[nodeId];
    return val === undefined ? 0 : Number(val);
  }

  /**
   * 设置字段在指定节点的权限值（auth）
   * @param item 表单组件规则
   * @param nodeId 流程节点ID
   * @param auth 权限值：0=可编辑, 1=不可见, 2=只读
   */
  function setFieldAuth(item: any, nodeId: string, auth: number) {
    if (!item || !nodeId) return;
    if (!item.nodePermissions) {
      item.nodePermissions = {};
    }
    item.nodePermissions[nodeId] = auth;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单设计器】流程节点字段权限控制核心逻辑---

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】移除节点级Schema配置，改为流程级（入口在流程列表）---
  // 节点级Schema配置入口已移至流程列表的「Schema」按钮，此处不再需要
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】移除节点级Schema配置，改为流程级（入口在流程列表）---

  // ===== XML 解析工具（用于从 processDef 加载节点）=====
  // update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】parseXmlNodes 仅支持蓝凌BPM格式，移除简单格式兼容---
  /** 蓝凌 BPM 节点类型 -> 中文标签映射 */
  const LANDRAY_NODE_LABELS: Record<string, string> = {
    startNode: '开始节点',
    draftNode: '起草节点',
    reviewNode: '审批节点',
    autoBranchNode: '条件分支',
    splitNode: '并行分支',
    joinNode: '汇合节点',
    endNode: '结束节点',
  };

  /**
   * 解析蓝凌 BPM 格式的 XML 文本，提取流程节点列表
   * 仅支持蓝凌BPM格式，不支持简单 <node> 格式
   */
  function parseXmlNodes(xmlText: string): ProcessNode[] {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xmlText, 'text/xml');
    const parseError = doc.querySelector('parsererror');
    if (parseError) {
      throw new Error('XML 格式错误：' + parseError.textContent?.substring(0, 100));
    }
    const nodes: ProcessNode[] = [];

    // 查找蓝凌 BPM 格式的节点元素（按固定顺序，保证节点顺序与流程定义一致）
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
        nodes.push({
          nodeId,
          name: name || LANDRAY_NODE_LABELS[tag] || nodeId,
          role: handlerNames || undefined,
          nodeType: tag,
        });
      }
    });

    if (nodes.length === 0) {
      throw new Error('未找到任何蓝凌BPM节点元素（startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode），请确认XML为蓝凌BPM格式');
    }
    return nodes;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单引擎】parseXmlNodes 仅支持蓝凌BPM格式，移除简单格式兼容---

  // ===== 构建导出JSON =====
  function buildSchemaJSON() {
    return schema.map((c) => ({
      type: c.type,
      field: c.field,
      title: c.title,
      props: { ...c.props },
      validate: [...c.validate],
      permissions: { ...c.permissions },
      nodePermissions: { ...(c.nodePermissions || {}) },
    }));
  }

  // ===== 保存 =====
  async function handleSave() {
    if (!processId.value) {
      createMessage.warning('缺少流程ID');
      return;
    }
    saving.value = true;
    try {
      // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】保存时仅持久化画布formSchema（字段定义已在流程列表维护）---
      await editProcess({
        id: processId.value,
        formSchema: buildSchemaJSON(),
      });
      // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】保存时仅持久化画布formSchema（字段定义已在流程列表维护）---
      createMessage.success('表单Schema保存成功');
    } catch (err) {
      console.error('[form-engine] save failed', err);
      createMessage.error('保存失败，请重试');
    } finally {
      saving.value = false;
    }
  }

  // ===== 导入/导出/预览弹窗 =====
  const [registerImportModal, { openModal: openImportModal, closeModal: closeImportModal }] = useModal();
  const [registerExportModal, { openModal: openExportModal }] = useModal();
  const [registerPreviewModal, { openModal: openPreviewModal }] = useModal();
  const importJsonText = ref('');
  const exportJsonText = computed(() => JSON.stringify(buildSchemaJSON(), null, 2));

  function handleOpenImport() {
    importJsonText.value = '';
    openImportModal(true);
  }

  function handleOpenExport() {
    openExportModal(true);
  }

  function handleOpenPreview() {
    if (schema.length === 0) {
      createMessage.warning('画布为空，请先添加组件');
      return;
    }
    // 初始化预览数据
    Object.keys(previewData).forEach((k) => delete previewData[k]);
    schema.forEach((item) => {
      if (item.field) {
        previewData[item.field] = item.props?.defaultValue || '';
      }
    });
    openPreviewModal(true);
  }

  function handleImportSubmit() {
    try {
      const parsed = JSON.parse(importJsonText.value);
      if (!Array.isArray(parsed)) {
        createMessage.error('JSON必须为数组格式');
        return;
      }
      schema.splice(0, schema.length);
      parsed.forEach((rule: any) => {
        schema.push({
          _id: nextId(),
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
        });
      });
      createMessage.success('导入成功');
      closeImportModal();
    } catch (e) {
      createMessage.error('JSON解析失败：' + (e instanceof Error ? e.message : String(e)));
    }
  }

  async function handleCopyExport() {
    try {
      await navigator.clipboard.writeText(exportJsonText.value);
      createMessage.success('已复制到剪贴板');
    } catch {
      createMessage.error('复制失败，请手动复制');
    }
  }

  function handleDownloadExport() {
    const blob = new Blob([exportJsonText.value], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `form-schema-${processId.value || 'export'}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    createMessage.success('文件已下载');
  }

  // ===== 返回列表 =====
  function handleBack() {
    router.push('/form-engine/list');
  }

  // ===== 加载流程数据 =====
  async function loadProcess() {
    if (!processId.value) return;
    try {
      const data: any = await getProcessById(processId.value);
      const formSchema = data?.formSchema || data?.result?.formSchema;
      if (formSchema) {
        const layout = Array.isArray(formSchema) ? formSchema : formSchema.layout;
        if (Array.isArray(layout) && layout.length) {
          schema.splice(0, schema.length);
          layout.forEach((rule: any) => {
            schema.push({
              _id: nextId(),
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
            });
          });
          if (schema.length) selectedId.value = schema[0]._id;
        }
      }
      // 从 processDef（XML）解析流程节点，用于字段权限配置
      const processDef = data?.processDef || data?.result?.processDef;
      if (processDef && typeof processDef === 'string' && processDef.trim()) {
        try {
          const nodes = parseXmlNodes(processDef);
          processNodes.value = nodes;
          if (nodes.length > 0) {
            configNodeId.value = nodes[0].nodeId;
          }
        } catch (e) {
          console.warn('[form-engine] parseXmlNodes from processDef failed', e);
        }
      }
      // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】加载流程级表单Schema字段（改用独立表接口）---
      // 从 fe_process_schema_field 独立表加载Schema字段定义
      try {
        const fieldsData: any = await getSchemaFields(processId.value);
        const fields = Array.isArray(fieldsData) ? fieldsData : (fieldsData?.result || []);
        if (Array.isArray(fields) && fields.length) {
          formSchemaFields.value = fields.map((f: any) => ({
            fieldKey: f.fieldKey,
            fieldLabel: f.fieldLabel,
            fieldType: f.fieldType,
            defaultValue: f.defaultValue || '',
            required: f.required === 1 || f.required === true,
            pattern: f.pattern || '',
            min: f.minValue ?? f.min,
            max: f.maxValue ?? f.max,
            validationMessage: f.validationMessage || '',
            description: f.description || '',
          }));
        }
      } catch (fieldsErr) {
        console.warn('[form-engine] getSchemaFields failed', fieldsErr);
      }
      // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】加载流程级表单Schema字段（改用独立表接口）---
    } catch (err) {
      console.error('[form-engine] getProcessById failed', err);
    }
  }

  // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】初始化渲染：根据Schema字段自动生成画布布局---
  /**
   * 根据已配置的表单Schema字段自动生成初始画布布局
   * 每个Schema字段映射为对应的表单组件（基于FIELD_TYPE_TO_COMPONENT）
   */
  async function autoGenerateLayoutFromSchema() {
    if (formSchemaFields.value.length === 0) return;
    schema.splice(0, schema.length);
    formSchemaFields.value.forEach((field) => {
      const allowedTypes = FIELD_TYPE_TO_COMPONENT[field.fieldType] || ['input'];
      const compType = allowedTypes[0];
      const validate: any[] = [];
      if (field.required) {
        validate.push({
          required: true,
          message: `${field.fieldLabel || field.fieldKey}为必填项`,
          trigger: 'blur',
        });
      }
      if (field.pattern) {
        try {
          validate.push({
            pattern: new RegExp(field.pattern),
            message: field.validationMessage || '格式不正确',
            trigger: 'blur',
          });
        } catch (e) {
          // 正则表达式无效时跳过
        }
      }
      schema.push({
        _id: nextId(),
        type: compType,
        field: field.fieldKey,
        title: field.fieldLabel || field.fieldKey,
        props: {
          placeholder: `请输入${field.fieldLabel || ''}`,
          defaultValue: field.defaultValue || '',
        },
        validate,
        permissions: {
          applicant: 'write',
          manager: 'readonly',
          finance: 'readonly',
        },
        nodePermissions: {},
      });
    });
    if (schema.length) {
      selectedId.value = schema[0]._id;
      createMessage.success(`已根据 ${schema.length} 个Schema字段自动生成表单布局`);
    }
  }
  // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】初始化渲染：根据Schema字段自动生成画布布局---

  // ===== 加载字段列表 =====
  async function loadFields() {
    const tid = templateId.value;
    if (!tid) return;
    try {
      const data: any = await getFormFieldList(tid);
      const list = Array.isArray(data) ? data : data?.records || data?.list || [];
      if (list.length) fieldList.value = list;
    } catch (err) {
      console.error('[form-engine] getFormFieldList failed', err);
    }
  }

  // ===== 加载方法列表 =====
  async function loadMethods() {
    try {
      const data: any = await getMethodInfo();
      const list = Array.isArray(data) ? data : data?.records || data?.list || [];
      if (list.length) methodList.value = list;
    } catch (err) {
      console.error('[form-engine] getMethodInfo failed', err);
    }
  }

  onMounted(async () => {
    // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】移除前置检查逻辑，直接进入设计器---
    if (!processId.value) {
      console.warn('[form-engine] 缺少流程ID');
      return;
    }
    // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】移除前置检查逻辑，直接进入设计器---

    // update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】初始化渲染：根据Schema自动生成画布布局---
    // 先加载流程数据（含已保存的formSchema），若画布为空则根据Schema自动生成初始布局
    await loadProcess();
    if (schema.length === 0 && formSchemaFields.value.length > 0) {
      // 画布为空但有Schema字段，自动根据Schema生成初始布局
      await autoGenerateLayoutFromSchema();
    }
    // update-end---author:formengine ---date:2026-07-15  for：【表单设计器】初始化渲染：根据Schema自动生成画布布局---
    await Promise.all([loadFields(), loadMethods()]);
  });
  // update-end---author:formengine ---date:2026-07-08  for：【表单设计器】全面完善：三栏骨架+拖拽+预览+复制+导出，模板语法替代h()---
</script>

<style scoped>
  .form-designer {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f0f2f5;
  }

  .designer-header {
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
    flex-wrap: wrap;
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
  /* 全局配置标签样式 */
  .global-config-label {
    font-size: 13px;
    color: #666;
    font-weight: 500;
    white-space: nowrap;
  }
  /* update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】访问控制拦截层样式--- */
  .precondition-blocker {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    background: #fafafa;
  }
  /* update-end---author:formengine ---date:2026-07-15  for：【表单设计器】访问控制拦截层样式--- */

  .designer-body {
    flex: 1;
    display: flex;
    overflow: hidden;
  }

  /* 左侧组件面板 */
  .designer-left {
    width: 240px;
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
  .component-list {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
  .component-group {
    margin-bottom: 16px;
  }
  .group-title {
    font-size: 12px;
    color: #999;
    margin-bottom: 8px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    user-select: none;
  }
  .group-arrow {
    font-size: 10px;
    transition: transform 0.2s;
  }
  .group-arrow.collapsed {
    transform: rotate(-90deg);
  }
  .group-items {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
  .component-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 10px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 12px;
  }
  .component-item:hover {
    background: #e6f4ff;
    border-color: #91caff;
    transform: translateY(-1px);
  }
  .component-item:active {
    transform: translateY(0);
  }
  .comp-icon {
    font-size: 14px;
    color: #1890ff;
  }
  .comp-label {
    color: #333;
  }

  /* 中间画布 */
  .designer-center {
    flex: 1;
    background: #f0f2f5;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .canvas-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    transition: background 0.2s;
  }
  .canvas-header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .canvas-area.drag-over {
    background: #e6f4ff;
    outline: 2px dashed #91caff;
    outline-offset: -4px;
  }
  .canvas-list {
    max-width: 700px;
    margin: 0 auto;
  }
  .canvas-item {
    background: #fff;
    border: 2px solid transparent;
    border-radius: 6px;
    padding: 12px 16px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: border-color 0.2s, box-shadow 0.2s;
  }
  .canvas-item:hover {
    border-color: #d6e4ff;
  }
  .canvas-item.selected {
    border-color: #4096ff;
    box-shadow: 0 0 0 3px rgba(64, 150, 255, 0.12);
  }
  /* 节点+角色动态展示状态：隐藏字段半透明 + 虚线边框 */
  .canvas-item.state-hidden {
    opacity: 0.3;
    border-style: dashed;
    border-color: #ffccc7;
    background: #fff1f0;
  }
  /* 节点+角色动态展示状态：只读字段灰色背景 */
  .canvas-item.state-readonly {
    background: #fafafa;
    border-color: #d6e4ff;
  }
  /* 节点+角色动态展示状态：可编辑字段绿色边框 */
  .canvas-item.state-editable {
    border-color: #b7eb8f;
    background: #f6ffed;
  }
  .canvas-item-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
  }
  .canvas-item-index {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    color: #666;
    flex-shrink: 0;
  }
  .item-title {
    font-weight: 500;
    font-size: 13px;
    flex: 1;
  }
  .canvas-item-spacer {
    flex: 1;
  }
  .canvas-item-actions {
    display: flex;
    gap: 2px;
    flex-shrink: 0;
  }
  .canvas-item-preview {
    padding-left: 32px;
    max-width: 500px;
  }

  /* 右侧属性面板（Tab切换布局） */
  .designer-right {
    width: 380px;
    background: #fff;
    border-left: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
  }
  .props-area {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }
  /* update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】Tab切换布局样式--- */
  .props-tab-container {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  .props-tabs {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  .props-tabs :deep(.ant-tabs-content) {
    flex: 1;
    overflow-y: auto;
    padding: 12px;
  }
  .props-tabs :deep(.ant-tabs-tabpane) {
    height: 100%;
  }
  /* update-end---author:formengine ---date:2026-07-15  for：【表单设计器】Tab切换布局样式--- */

  .perm-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 12px;
    margin-top: 8px;
  }
  .perm-table th,
  .perm-table td {
    border: 1px solid #f0f0f0;
    padding: 6px 4px;
    text-align: center;
  }
  .perm-table th {
    background: #fafafa;
    color: #666;
    font-weight: 600;
  }
  .perm-table .role-cell {
    text-align: left;
    padding-left: 8px;
  }
  .perm-table .node-cell {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
  }
  .perm-table .node-id-text {
    font-size: 11px;
    color: #999;
    font-family: monospace;
  }

  /* JSON 预览 */
  .json-preview {
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

  /* 预览弹窗 */
  .preview-container {
    max-width: 900px;
    margin: 0 auto;
  }
  .preview-data {
    margin-top: 16px;
  }
  .preview-data-title {
    font-weight: 600;
    margin-bottom: 8px;
    color: #666;
  }
  .json-preview-small {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
    overflow: auto;
    max-height: 300px;
    margin: 0;
  }

  /* update-begin---author:formengine ---date:2026-07-15  for：【表单设计器】流程级Schema字段预览样式--- */
  .schema-fields-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding: 8px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    background: #fafafa;
    margin-bottom: 12px;
  }
  /* update-end---author:formengine ---date:2026-07-15  for：【表单设计器】流程级Schema字段预览样式--- */
</style>
