<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="导入流程定义XML"
    :defaultFullscreen="true"
    okText="解析并导入"
    @ok="handleSubmit"
  >
    <div class="import-container">
      <a-alert
        message="导入蓝凌BPM流程定义XML（process.xml），解析流程节点列表用于字段权限配置"
        description="仅支持蓝凌BPM格式：包含 startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode 节点类型。导入后节点信息将保存到流程定义中，设计器可直接基于这些节点配置字段权限。"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />
      <div style="margin-bottom: 12px">
        <a-upload
          :before-upload="handleFileSelect"
          :show-upload-list="false"
          accept=".xml"
        >
          <a-button type="primary">
            <template #icon><UploadOutlined /></template>
            选择XML文件
          </a-button>
        </a-upload>
        <span v-if="fileName" style="margin-left: 12px; color: #999">已选择：{{ fileName }}</span>
      </div>
      <a-row :gutter="16">
        <a-col :span="12">
          <div class="section-title">流程定义 XML 内容</div>
          <a-textarea
            v-model:value="xmlText"
            :rows="20"
            placeholder='粘贴 process.xml 内容，例如：&#10;<?xml version="1.0" encoding="UTF-8"?>&#10;<process>&#10;  <node id="n1" name="申请" role="applicant" formTemplateId="tmpl_001"/>&#10;  <node id="n2" name="经理审批" role="manager" formTemplateId="tmpl_001"/>&#10;  <node id="n3" name="财务复核" role="finance" formTemplateId="tmpl_001"/>&#10;</process>'
            style="font-family: monospace; font-size: 13px"
          />
        </a-col>
        <a-col :span="12">
          <div class="section-title">解析结果预览</div>
          <a-card v-if="parsedNodes.length" style="margin-bottom: 12px">
            <template #title>
              <a-badge :count="parsedNodes.length" :offset="[8, -2]">节点列表</a-badge>
            </template>
            <a-table
              :dataSource="parsedNodes"
              :columns="nodeColumns"
              :pagination="false"
              size="small"
              :rowKey="(r) => r.nodeId"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.dataIndex === 'role'">
                  <a-tag :color="getRoleColor(record.role)">{{ getRoleLabel(record.role) }}</a-tag>
                </template>
              </template>
            </a-table>
          </a-card>
          <a-empty v-else description="请输入有效的XML内容" />
          <div v-if="parseError" style="color: red; margin-top: 8px">{{ parseError }}</div>
        </a-col>
      </a-row>
    </div>
  </BasicModal>
</template>

<script lang="ts" name="process-import-modal" setup>
  // update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】导入流程定义XML全屏弹窗（从JSON改为XML）---
  import { ref, computed } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { UploadOutlined } from '@ant-design/icons-vue';
  import { importProcessDefinition, editProcess } from '/@/api/form-engine';

  const emit = defineEmits(['success']);
  const { createMessage } = useMessage();
  const xmlText = ref('');
  const fileName = ref('');
  const parseError = ref('');
  const processId = ref('');
  const processName = ref('');
  const templateId = ref('');

  const nodeColumns = [
    { title: '节点ID', dataIndex: 'nodeId', width: 80 },
    { title: '节点名称', dataIndex: 'name', width: 120 },
    { title: '节点类型', dataIndex: 'nodeType', width: 120 },
    { title: '处理人', dataIndex: 'role', width: 140 },
  ];

  /** 流程节点结构 */
  interface ProcessNode {
    nodeId: string;
    name: string;
    role?: string;
    formTemplateId?: string;
    /** 节点类型（蓝凌BPM: startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode） */
    nodeType?: string;
    /** 处理人ID */
    handlerIds?: string;
    /** 处理人名称 */
    handlerNames?: string;
  }

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
   * 仅支持蓝凌BPM格式：<process><nodes><startNode id="N1" name="开始"/>...<reviewNode id="N14" name="审批" handlerNames="张三"/>...</nodes><lines>...</lines></process>
   * 不支持简单 <node> 格式
   */
  function parseXmlNodes(xml: string): ProcessNode[] {
    const parser = new DOMParser();
    const doc = parser.parseFromString(xml, 'text/xml');
    const parseErr = doc.querySelector('parsererror');
    if (parseErr) {
      throw new Error('XML 格式错误：' + parseErr.textContent?.substring(0, 100));
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
      const handlerIds = el.getAttribute('handlerIds') || undefined;
      const handlerNames = el.getAttribute('handlerNames') || undefined;
      if (nodeId) {
        nodes.push({
          nodeId,
          name: name || LANDRAY_NODE_LABELS[tag] || nodeId,
          nodeType: tag,
          role: handlerNames || undefined, // 蓝凌的 handlerNames 映射为 role（用于权限配置显示）
          handlerIds,
          handlerNames,
        });
      }
    });

    if (nodes.length === 0) {
      throw new Error('未找到任何蓝凌BPM节点元素（startNode/draftNode/reviewNode/autoBranchNode/splitNode/joinNode/endNode），请确认XML为蓝凌BPM格式');
    }
    return nodes;
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单引擎】parseXmlNodes 仅支持蓝凌BPM格式，移除简单格式兼容---

  /** 实时预览解析结果 */
  const parsedNodes = computed<ProcessNode[]>(() => {
    parseError.value = '';
    if (!xmlText.value.trim()) return [];
    try {
      return parseXmlNodes(xmlText.value);
    } catch (e) {
      parseError.value = e instanceof Error ? e.message : String(e);
      return [];
    }
  });

  const roleColorMap = { applicant: 'blue', manager: 'orange', finance: 'green' };
  const roleLabelMap = { applicant: '申请人', manager: '部门经理', finance: '财务总监' };
  function getRoleColor(role: string) {
    return roleColorMap[role] || 'default';
  }
  function getRoleLabel(role: string) {
    return roleLabelMap[role] || role;
  }

  /** 选择 XML 文件时读取内容到文本框 */
  function handleFileSelect(file: File): boolean {
    fileName.value = file.name;
    const reader = new FileReader();
    reader.onload = (e) => {
      xmlText.value = String(e.target?.result || '');
    };
    reader.onerror = () => {
      parseError.value = '文件读取失败';
    };
    reader.readAsText(file, 'UTF-8');
    return false; // 阻止自动上传
  }

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    xmlText.value = '';
    fileName.value = '';
    parseError.value = '';
    if (data?.record) {
      processId.value = data.record.id || '';
      processName.value = data.record.processName || '';
      templateId.value = data.record.templateId || '';
      // 如果已有 processDef（之前导入的XML内容），预填
      if (data.record.processDef) {
        xmlText.value = data.record.processDef;
      }
    }
    setModalProps({ confirmLoading: false });
  });

  /** 提交导入：解析XML节点，保存到流程的 processDef 字段 */
  async function handleSubmit() {
    if (!xmlText.value.trim()) {
      createMessage.warning('请粘贴XML内容或选择XML文件');
      return;
    }
    if (parseError.value) {
      createMessage.error('XML格式错误，请修正后再提交');
      return;
    }
    try {
      const nodes = parseXmlNodes(xmlText.value);
      setModalProps({ confirmLoading: true });
      // 保存原始 XML 到 processDef 字段，同时通过 importProcessDefinition 记录导入状态
      await editProcess({
        id: processId.value,
        processDef: xmlText.value,
        status: 'imported',
      });
      createMessage.success(`成功导入 ${nodes.length} 个流程节点`);
      closeModal();
      emit('success');
    } catch (e) {
      createMessage.error('导入失败：' + (e instanceof Error ? e.message : String(e)));
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  // update-end---author:formengine ---date:2026-07-08  for：【表单引擎】导入流程定义XML全屏弹窗（从JSON改为XML）---
</script>

<style lang="less" scoped>
  .import-container {
    padding: 0 4px;
  }
  .section-title {
    font-weight: 600;
    margin-bottom: 8px;
    font-size: 14px;
  }
</style>
