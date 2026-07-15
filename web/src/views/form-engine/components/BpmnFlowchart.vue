<template>
  <div class="bpmn-flowchart-container">
    <div class="flowchart-toolbar">
      <span class="toolbar-title">BPMN 流程图</span>
      <span class="toolbar-info">节点：{{ nodes.length }} | 连线：{{ lines.length }}</span>
      <div class="toolbar-actions">
        <a-button size="small" @click="zoomIn"><template #icon><ZoomInOutlined /></template>放大</a-button>
        <a-button size="small" @click="zoomOut"><template #icon><ZoomOutOutlined /></template>缩小</a-button>
        <a-button size="small" @click="resetZoom"><template #icon><CompressOutlined /></template>重置</a-button>
      </div>
    </div>
    <div class="flowchart-svg-wrapper" ref="wrapperRef">
      <svg
        :width="svgWidth"
        :height="svgHeight"
        :viewBox="viewBox"
        class="flowchart-svg"
      >
        <!-- update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】BPMN流程图SVG渲染---
        defs：定义箭头标记、节点样式渐变 -->
        <defs>
          <!-- 普通箭头 -->
          <marker id="arrow-default" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L0,6 L9,3 z" fill="#999" />
          </marker>
          <!-- 高亮箭头（当前流转路径） -->
          <marker id="arrow-active" markerWidth="10" markerHeight="10" refX="9" refY="3" orient="auto" markerUnits="strokeWidth">
            <path d="M0,0 L0,6 L9,3 z" fill="#1890ff" />
          </marker>
        </defs>

        <!-- 渲染连线 -->
        <g class="lines-layer">
          <g v-for="line in renderedLines" :key="line.id" class="line-group">
            <!-- 连线折线 -->
            <polyline
              :points="line.svgPoints"
              :class="['flow-line', { 'flow-line-active': isLineActive(line) }]"
              fill="none"
              :marker-end="isLineActive(line) ? 'url(#arrow-active)' : 'url(#arrow-default)'"
            />
            <!-- 分支条件标签 -->
            <text
              v-if="line.name"
              :x="line.labelX"
              :y="line.labelY"
              class="line-label"
            >
              {{ line.name }}
            </text>
          </g>
        </g>

        <!-- 渲染节点 -->
        <g class="nodes-layer">
          <g
            v-for="node in renderedNodes"
            :key="node.id"
            :transform="`translate(${node.x}, ${node.y})`"
            :class="['node-group', { 'node-current': node.id === currentNodeId, 'node-passed': passedNodeIds.includes(node.id) }]"
            @click="$emit('nodeClick', node)"
          >
            <!-- 开始/结束节点：圆形 -->
            <template v-if="node.type === 'startNode' || node.type === 'endNode'">
              <circle
                :r="nodeRadius"
                :class="['node-shape', `node-${node.type}`]"
              />
              <text class="node-text" text-anchor="middle" dy="5">{{ node.name }}</text>
            </template>

            <!-- 条件分支节点：菱形 -->
            <template v-else-if="node.type === 'autoBranchNode'">
              <polygon
                :points="diamondPoints"
                :class="['node-shape', 'node-branch']"
              />
              <text class="node-text" text-anchor="middle" dy="5">{{ node.name }}</text>
            </template>

            <!-- 审批/起草/并行节点：圆角矩形 -->
            <template v-else>
              <rect
                :x="-rectWidth / 2"
                :y="-rectHeight / 2"
                :width="rectWidth"
                :height="rectHeight"
                rx="6"
                ry="6"
                :class="['node-shape', `node-${node.type}`]"
              />
              <!-- 节点名称 -->
              <text class="node-text" text-anchor="middle" :dy="node.handler ? -6 : 5">{{ node.name }}（{{ node.id }}）</text>
              <!-- 处理人 -->
              <text v-if="node.handler" class="node-handler" text-anchor="middle" dy="14">
                {{ truncateHandler(node.handler) }}
              </text>
            </template>
          </g>
        </g>
      </svg>
    </div>
    <!-- 图例 -->
    <div class="flowchart-legend">
      <div class="legend-item"><span class="legend-shape legend-start"></span>开始节点</div>
      <div class="legend-item"><span class="legend-shape legend-draft"></span>起草节点</div>
      <div class="legend-item"><span class="legend-shape legend-review"></span>审批节点</div>
      <div class="legend-item"><span class="legend-shape legend-branch"></span>条件分支</div>
      <div class="legend-item"><span class="legend-shape legend-split"></span>并行分支</div>
      <div class="legend-item"><span class="legend-shape legend-end"></span>结束节点</div>
      <div class="legend-item"><span class="legend-shape legend-current"></span>当前节点</div>
      <div class="legend-item"><span class="legend-shape legend-passed"></span>已流转</div>
    </div>
  </div>
</template>

<script lang="ts" setup>
// update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】BPMN流程图组件，解析蓝凌BPM XML渲染SVG流程图---
import { computed, ref } from 'vue';
import { ZoomInOutlined, ZoomOutOutlined, CompressOutlined } from '@ant-design/icons-vue';

/** 蓝凌BPM节点类型中文标签映射 */
const LANDRAY_NODE_LABELS: Record<string, string> = {
  startNode: '开始节点',
  draftNode: '起草节点',
  reviewNode: '审批节点',
  autoBranchNode: '条件分支',
  splitNode: '启动并行分支',
  joinNode: '结束并行分支',
  endNode: '结束节点',
};

/** 流程节点 */
interface FlowNode {
  id: string;
  name: string;
  type: string;
  typeLabel: string;
  x: number;
  y: number;
  handler?: string;
}

/** 流程连线 */
interface FlowLine {
  id: string;
  startNodeId: string;
  endNodeId: string;
  points: string;        // 原始 points 字符串 "x1,y1;x2,y2"
  svgPoints: string;     // SVG polyline points 格式 "x1,y1 x2,y2"
  name?: string;         // 分支条件标签（是/否）
  labelX: number;
  labelY: number;
}

/** Props */
const props = defineProps<{
  xmlContent: string;           // 蓝凌BPM XML 字符串
  currentNodeId?: string;       // 当前节点ID（高亮）
  passedNodeIds?: string[];     // 已流转节点ID列表
}>();

/** Emits */
defineEmits<{
  (e: 'nodeClick', node: FlowNode): void;
}>();

// 节点尺寸常量
const rectWidth = 140;
const rectHeight = 56;
const nodeRadius = 26;
const diamondPoints = '0,-32 40,0 0,32 -40,0'; // 菱形顶点

// 缩放控制
const zoom = ref(1);
const svgWidth = ref('100%');
const svgHeight = ref('600px');

/** 解析 XML 提取节点 */
const nodes = computed<FlowNode[]>(() => {
  if (!props.xmlContent) return [];
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(props.xmlContent, 'text/xml');
    const nodeElements: Element[] = [];
    // 蓝凌BPM 7种节点类型
    const nodeTypes = ['startNode', 'draftNode', 'reviewNode', 'autoBranchNode', 'splitNode', 'joinNode', 'endNode'];
    nodeTypes.forEach((type) => {
      const elements = doc.querySelectorAll(type);
      elements.forEach((el) => nodeElements.push(el));
    });
    return nodeElements.map((el) => {
      const id = el.getAttribute('id') || '';
      const name = el.getAttribute('name') || LANDRAY_NODE_LABELS[el.tagName] || el.tagName;
      const x = parseFloat(el.getAttribute('x') || '0');
      const y = parseFloat(el.getAttribute('y') || '0');
      // handlerNames 可能是处理人名，也可能是公式（$组织架构...$）
      const handler = el.getAttribute('handlerNames') || '';
      return {
        id,
        name,
        type: el.tagName,
        typeLabel: LANDRAY_NODE_LABELS[el.tagName] || el.tagName,
        x,
        y,
        handler: handler && !handler.startsWith('$') ? handler : (handler ? '公式' : ''),
      };
    });
  } catch (e) {
    return [];
  }
});

/** 解析 XML 提取连线 */
const lines = computed<FlowLine[]>(() => {
  if (!props.xmlContent) return [];
  try {
    const parser = new DOMParser();
    const doc = parser.parseFromString(props.xmlContent, 'text/xml');
    const lineElements = doc.querySelectorAll('line');
    return Array.from(lineElements).map((el) => {
      const points = el.getAttribute('points') || '';
      // 蓝凌格式 "x1,y1;x2,y2" -> SVG polyline "x1,y1 x2,y2"
      const svgPoints = points.replace(/;/g, ' ');
      const name = el.getAttribute('name') || '';
      // 计算标签位置（取连线中点偏移）
      const coordPairs = points.split(';').map((p) => {
        const [x, y] = p.split(',').map(Number);
        return { x, y };
      });
      const midIndex = Math.floor(coordPairs.length / 2);
      const midPoint = coordPairs[midIndex] || { x: 0, y: 0 };
      return {
        id: el.getAttribute('id') || '',
        startNodeId: el.getAttribute('startNodeId') || '',
        endNodeId: el.getAttribute('endNodeId') || '',
        points,
        svgPoints,
        name,
        labelX: midPoint.x + 8,
        labelY: midPoint.y - 8,
      };
    });
  } catch (e) {
    return [];
  }
});

/** 计算边界并生成 viewBox */
const viewBox = computed(() => {
  if (nodes.value.length === 0) return '0 0 800 600';
  const xs = nodes.value.map((n) => n.x);
  const ys = nodes.value.map((n) => n.y);
  const minX = Math.min(...xs) - 80;
  const minY = Math.min(...ys) - 60;
  const maxX = Math.max(...xs) + 80;
  const maxY = Math.max(...ys) + 80;
  const w = (maxX - minX) * zoom.value;
  const h = (maxY - minY) * zoom.value;
  return `${minX} ${minY} ${w} ${h}`;
});

/** 渲染用节点（应用缩放） */
const renderedNodes = computed(() => {
  return nodes.value.map((n) => ({ ...n }));
});

/** 渲染用连线 */
const renderedLines = computed(() => {
  return lines.value;
});

/** 当前节点已流转节点列表 */
const currentNodeId = computed(() => props.currentNodeId || '');
const passedNodeIds = computed(() => props.passedNodeIds || []);

/** 判断连线是否为当前流转路径 */
function isLineActive(line: FlowLine): boolean {
  if (!currentNodeId.value) return false;
  return line.startNodeId === currentNodeId.value || line.endNodeId === currentNodeId.value;
}

/** 截断过长的处理人名 */
function truncateHandler(handler: string): string {
  return handler.length > 12 ? handler.substring(0, 12) + '...' : handler;
}

// 缩放操作
function zoomIn() {
  zoom.value = Math.min(zoom.value * 1.2, 3);
}
function zoomOut() {
  zoom.value = Math.max(zoom.value / 1.2, 0.3);
}
function resetZoom() {
  zoom.value = 1;
}
// update-end---author:formengine ---date:2026-07-08  for：【表单引擎】BPMN流程图组件，解析蓝凌BPM XML渲染SVG流程图---
</script>

<style lang="less" scoped>
.bpmn-flowchart-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  overflow: hidden;
}

.flowchart-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .toolbar-title {
    font-weight: 600;
    font-size: 14px;
  }
  .toolbar-info {
    color: #999;
    font-size: 12px;
  }
  .toolbar-actions {
    margin-left: auto;
    display: flex;
    gap: 4px;
  }
}

.flowchart-svg-wrapper {
  flex: 1;
  overflow: auto;
  background: #fafafa;
  // 网格背景
  background-image:
    linear-gradient(rgba(0,0,0,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,0,0,0.03) 1px, transparent 1px);
  background-size: 20px 20px;
}

.flowchart-svg {
  display: block;
}

/* 连线样式 */
.flow-line {
  stroke: #999;
  stroke-width: 1.5;
  transition: stroke 0.3s;
}
.flow-line-active {
  stroke: #1890ff;
  stroke-width: 2.5;
}
.line-label {
  font-size: 12px;
  fill: #666;
  background: #fff;
}

/* 节点形状基础样式 */
.node-shape {
  stroke: #555;
  stroke-width: 1.5;
  fill: #fff;
  transition: all 0.3s;
}

/* 各类型节点颜色 */
.node-startNode {
  fill: #d9f7be;
  stroke: #52c41a;
}
.node-endNode {
  fill: #ffccc7;
  stroke: #ff4d4f;
}
.node-draftNode {
  fill: #bae7ff;
  stroke: #1890ff;
}
.node-reviewNode {
  fill: #e6f7ff;
  stroke: #1890ff;
}
.node-autoBranchNode {
  fill: #fff7e6;
  stroke: #fa8c16;
}
.node-splitNode {
  fill: #f9f0ff;
  stroke: #722ed1;
}
.node-joinNode {
  fill: #f9f0ff;
  stroke: #722ed1;
}

/* 当前节点高亮 */
.node-current .node-shape {
  stroke: #1890ff;
  stroke-width: 3;
  filter: drop-shadow(0 0 6px rgba(24, 144, 255, 0.5));
}

/* 已流转节点 */
.node-passed .node-shape {
  opacity: 0.7;
}

.node-group {
  cursor: pointer;
}
.node-text {
  font-size: 12px;
  fill: #333;
  pointer-events: none;
  user-select: none;
}
.node-handler {
  font-size: 10px;
  fill: #999;
  pointer-events: none;
  user-select: none;
}

/* 图例 */
.flowchart-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  padding: 8px 12px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  font-size: 12px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #666;
  }
  .legend-shape {
    display: inline-block;
    width: 16px;
    height: 12px;
    border-radius: 2px;
    border: 1.5px solid;
  }
  .legend-start { background: #d9f7be; border-color: #52c41a; border-radius: 50%; }
  .legend-draft { background: #bae7ff; border-color: #1890ff; }
  .legend-review { background: #e6f7ff; border-color: #1890ff; }
  .legend-branch { background: #fff7e6; border-color: #fa8c16; transform: rotate(45deg); width: 10px; height: 10px; }
  .legend-split { background: #f9f0ff; border-color: #722ed1; }
  .legend-end { background: #ffccc7; border-color: #ff4d4f; border-radius: 50%; }
  .legend-current { background: #fff; border-color: #1890ff; border-width: 3px; }
  .legend-passed { background: #f0f0f0; border-color: #d9d9d9; opacity: 0.7; }
}
</style>
