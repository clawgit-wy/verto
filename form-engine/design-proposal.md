# 表单引擎与流程仿真测试工作台 —— 详细设计方案 v2

> 本方案在 v1 基础上重构，采用 **jeecg-boot 式流程列表为中心入口** 的架构模式：
> 流程列表的**每一行**集成表单设计、流程配置导入、仿真测试、代码导出四大能力入口。
> 表单引擎以开源 **form-create** 设计器为基座，适配 **TanStack Form** 运行时，实现跨 UI 框架兼容。

---

## 一、系统定位与设计目标

### 1.1 定位
本工具是商业化流程引擎（Flowable / Activiti / Camunda / 厂商私有 BPM）的**前端配套工作台**，对标 jeecg-boot 的"在线表单 + 在线流程 + 表单挂接"三位一体模式：

- **流程引擎**负责流程编排、节点流转、持久化、回退/会签等后端能力；
- **本工具**负责：以**流程列表**为组织主线，每行流程绑定一个表单模板，提供"设计表单 → 导入流程配置 → 仿真测试 → 导出代码"的闭环。

### 1.2 与 jeecg-boot 架构模式的对照
| jeecg-boot 能力 | 本工具对应实现 |
|----------------|---------------|
| Online 表单设计器 | 基于 form-create 的可视化设计器（Design Mode） |
| Flowable 流程设计 + 表单挂接 | 流程配置导入 + 节点-表单-角色绑定（列表行级入口） |
| 颗粒化权限（行/列/字段级） | 字段级权限矩阵（write/readonly/hidden）× 角色 × 节点 |
| 代码生成器 | 基于 Schema 生成 Vue3 SFC（Export Mode） |
| 流程运行/监控 | 多角色仿真沙箱（Simulation Mode） |

### 1.3 核心目标
| 目标 | 说明 |
|------|------|
| 流程列表为中心 | 类 jeecg-boot 列表页，每行聚合四大能力入口 |
| 行级集成表单设计 | 每行流程可直接打开表单设计器，绑定字段、配置权限节点 |
| 流程配置导入 | 从流程引擎导入流程定义（节点 + 表单绑定关系） |
| 仿真测试入口 | 每行提供独立"仿真测试"按钮，进入多角色沙箱 |
| 代码导出入口 | 每行提供"导出代码"按钮，生成可集成 Vue3 SFC |
| 跨 UI 框架兼容 | 表单引擎适配 TanStack Form，支持 Element Plus / Ant Design Vue / Naive UI |

---

## 二、系统架构

### 2.1 总体架构（以流程列表为枢纽）

```
┌──────────────────────────────────────────────────────────────────┐
│                      流程列表 (Process List) ← 主入口              │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┬──────┐ │
│  │ 流程信息  │ 表单状态  │ 设计表单  │ 导入流程  │ 仿真测试  │ 导出 │ │
│  │ 名称/编码 │ 模板/版本 │ → Design │ → Import │→Simulation│→Export│ │
│  └──────────┴──────────┴──────────┴──────────┴──────────┴──────┘ │
├──────────────────────────────────────────────────────────────────┤
│                         四大功能模块                               │
│  ① 表单设计器 ② 流程配置导入 ③ 仿真测试沙箱 ④ 代码导出            │
├──────────────────────────────────────────────────────────────────┤
│                      表单引擎核心 (Form Engine)                   │
│  form-create 设计器 │ Schema 转换层 │ TanStack Form 运行时 │ Renderer│
├──────────────────────────────────────────────────────────────────┤
│                      契约适配层 (API Contracts)                    │
│  getTemplateFormList │ getFormFieldList │ getFormFieldValueList   │
│  getMethodInfo │ doMethodProcess │ importProcessDefinition        │
├──────────────────────────────────────────────────────────────────┤
│                      数据/集成层 (Data & Integration)              │
│  Mock Server │ 流程引擎适配器 │ 代码生成器 │ IndexedDB 持久化       │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 与流程引擎的协作边界（松耦合）
- **本工具不实现**：BPMN 解析、节点路由、任务持久化、组织模型。
- **本工具实现**：表单 Schema 编排、字段-流程变量映射、角色权限矩阵、仿真时序编排、代码生成。
- **对接方式**：流程引擎导出 `processDefinition`（节点 + 表单绑定关系），本工具**导入**后用于仿真时序与权限矩阵配置。这与 jeecg-boot 的"表单挂接"机制一致——流程与表单解耦，通过绑定关系关联。

---

## 三、表单引擎选型与 TanStack Form 适配（核心决策）

### 3.1 开源表单设计器选型评估

| 项目 | 技术栈 | 跨 UI 框架 | Schema 驱动 | 权限控制 | 可商用 | 适配 TanStack Form 难度 |
|------|--------|-----------|------------|---------|--------|----------------------|
| **form-create** | Vue2/3 | ✅ 5 大 UI 库 | ✅ JSON | ✅ v3.3 内置 | ✅ MIT | ★☆☆ 低 |
| Formily | React/Vue2/3/RN | 部分 | ✅ JSON Schema | 需扩展 | ✅ MIT | ★★★ 高（协议复杂） |
| Variant Form (VForm3) | Vue3 + Element Plus | ❌ 单一 | ✅ JSON | 需扩展 | 部分 | ★★☆ 中 |
| FormMaking | Vue2/3 | Element+AntD | ✅ JSON | 需扩展 | 双版本 | ★★☆ 中 |
| Form.io | 纯 JS | ✅ 框架无关 | ✅ JSON | ✅ | ✅ MIT | ★★☆ 中 |

**决策：选用 form-create 作为设计器基座**，理由：
1. **原生多 UI 框架支持**：Element / Ant Design Vue / iView / Naive UI 等，与"跨 UI 框架兼容"目标天然契合。
2. **JSON Schema 驱动**：设计器产出标准 JSON，便于转换到 TanStack Form 的 field 配置。
3. **v3.3 内置权限控制**：字段级 disabled/readonly/hidden，与权限矩阵需求一致。
4. **设计器与渲染器分离**：`FcDesigner` 产出 JSON，`FormCreate` 渲染——可在渲染层替换为 TanStack Form。
5. **开源 MIT 可商用**，社区活跃。

### 3.2 TanStack Form 适配方案

TanStack Form 是 100% headless、框架无关的表单状态管理库（`@tanstack/vue-form`），提供细粒度订阅与深度类型安全。适配思路：**form-create 产出 Schema → 转换层 → TanStack Form 运行时 → 任意 UI 框架渲染**。

```
┌─────────────┐    JSON Schema    ┌──────────────┐  field config  ┌──────────────┐
│ form-create │ ───────────────▶ │ Schema Adapter│ ─────────────▶ │ TanStack Form│
│  Designer   │                   │ (schema→field)│                │  useForm     │
└─────────────┘                   └──────────────┘                │  <Field>     │
       ▲                                                          └──────┬───────┘
       │ 反向同步（运行时改值回写）                                       │ headless
       └─────────────────────────────────────────────────────────────┘
                                                                  │
                                                  ┌───────────────▼───────────────┐
                                                  │  UI Adapter (可插拔)           │
                                                  │  Element Plus / Ant Design Vue │
                                                  │  / Naive UI / shadcn-vue       │
                                                  └───────────────────────────────┘
```

**Schema Adapter 转换规则**：
| form-create Schema | TanStack Form |
|--------------------|---------------|
| `field: "bus_type"` | `form.Field name="bus_type"` |
| `props.required: true` | `validators.onSubmit: z.string().min(1)` |
| `props.disabled: true` | 渲染层 `:disabled` |
| `validate: [{pattern, message}]` | `validators.onChange` |
| `control: "input"` | UI Adapter 映射到 `<el-input>` / `<a-input>` |
| 权限矩阵 `permissions[role]` | 运行时计算 disabled/hidden，注入 field |

**跨 UI 框架兼容的关键**：UI Adapter 层可插拔。同一份 Schema + 同一个 TanStack Form 实例，通过切换 Adapter 即可渲染到不同 UI 框架，导出代码时按目标项目 UI 库生成对应标签。

---

## 四、功能模块详细设计

### 4.1 流程列表模块（主入口，类 jeecg-boot）

#### 4.1.1 列表页结构
- **顶部**：搜索框（流程名称/编码）、"新建流程"按钮、UI 框架切换（影响设计与导出）。
- **表格列**：
  | 列 | 说明 |
  |----|------|
  | 流程名称 / 编码 | 如"客车采购申请流程 / PROC_BUS_001" |
  | 表单模板 | 绑定的 templateId / templateName / version |
  | 流程状态 | 未导入 / 已导入 / 已仿真 / 已导出（彩色标签） |
  | 更新时间 | — |
  | 操作 | 设计表单 \| 导入流程 \| 仿真测试 \| 导出代码 \| 更多(删除/复制) |

#### 4.1.2 行级操作（核心）
每一行集成四大能力入口，点击即进入对应模式并携带当前流程上下文：
- **设计表单** → 打开表单设计器（Design Mode），加载该行表单模板 Schema。
- **导入流程** → 弹窗：从流程引擎导入流程定义 JSON，配置节点-表单-角色绑定。
- **仿真测试** → 打开仿真沙箱（Simulation Mode），基于该流程的节点与权限矩阵。
- **导出代码** → 打开代码导出（Export Mode），基于该行表单 Schema 生成 SFC。

### 4.2 表单设计器模块（Design Mode）
基于 form-create 设计器，扩展以下能力：
- **字段绑定**：下拉绑定 `getFormFieldList` 的 `fieldKey`。
- **动作绑定**：按钮组件选择 `getMethodInfo` 的 `methodKey`，映射入参字段。
- **权限节点配置**：按角色 × 节点配置 `write / readonly / hidden`（与流程节点联动）。
- **可见性条件**：表达式控制显隐。
- **Schema 双向同步**：设计器修改 → 更新 TanStack Form；运行时改值 → 回写设计器。

### 4.3 流程配置导入模块（Import）
- **导入来源**：流程引擎导出的 `processDefinition` JSON（节点列表 + 表单绑定）。
- **导入后配置**：
  - 节点列表：每个节点关联表单模板、角色、可触发业务方法。
  - 权限矩阵：按节点 × 角色 × 字段配置权限。
- **校验**：导入时校验节点-表单绑定完整性，缺失则提示。

### 4.4 仿真测试模块（Simulation Mode）
- 角色切换（申请人 → 部门经理 → 财务总监），权限矩阵动态生效。
- "模拟流转/下一步"推进节点，暂存表单数据。
- 业务方法触发：`doMethodProcess` 模拟执行，可改写字段值。
- 异常注入：必填校验失败、业务方法异常、越权操作。
- 测试回放：记录每步轨迹（角色、节点、操作、快照），支持回放。

### 4.5 代码导出模块（Export Mode）
- 实时预览：基于 Schema + TanStack Form 生成 Vue3 SFC。
- 选项：UI 库（Element Plus / Ant Design Vue / Naive UI）、代码风格（script setup / Options API）、是否含校验/权限指令/业务事件占位函数。
- 导出内容：表单结构、`v-model` 绑定、权限矩阵计算函数、`doMethodProcess` 占位函数、TanStack Form 集成代码。
- 下载 `.vue` 文件 + 在线渲染校验。

---

## 五、数据模型设计

### 5.1 流程列表项（行数据）
```jsonc
{
  "processId": "proc_001",
  "processName": "客车采购申请流程",
  "processCode": "PROC_BUS_001",
  "templateId": "tmpl_001",
  "templateName": "客车采购申请表单",
  "version": "v1.0",
  "status": "imported",        // draft / imported / simulated / exported
  "formSchema": { /* 见 5.2 */ },
  "processDef": { /* 见 5.3，导入后填充 */ },
  "updatedAt": "2026-07-06T10:00:00Z"
}
```

### 5.2 表单 Schema（form-create 兼容 + 扩展）
```jsonc
{
  "templateId": "tmpl_001",
  "templateName": "客车采购申请表单",
  "version": "v1.0",
  "layout": [
    {
      "id": "comp_1",
      "type": "input",
      "field": "bus_type",          // form-create 字段名
      "title": "客车型号",
      "props": { "placeholder": "请输入", "required": true },
      "validate": [{ "required": true, "message": "客车型号不能为空" }],
      "permissions": {              // 扩展：权限矩阵
        "applicant": "write",
        "manager": "readonly",
        "finance": "readonly"
      },
      "visibleRule": ""
    },
    {
      "id": "comp_btn",
      "type": "button",
      "title": "检查库存",
      "action": {
        "methodKey": "stockService.checkInventory",
        "params": ["bus_type", "order_count"]
      }
    }
  ]
}
```

### 5.3 流程定义（导入自流程引擎）
```jsonc
{
  "processDefId": "pd_bus_purchase",
  "processName": "客车采购申请流程",
  "nodes": [
    { "nodeId": "n1", "name": "申请", "role": "applicant", "formTemplateId": "tmpl_001" },
    { "nodeId": "n2", "name": "经理审批", "role": "manager", "formTemplateId": "tmpl_001" },
    { "nodeId": "n3", "name": "财务复核", "role": "finance", "formTemplateId": "tmpl_001" }
  ],
  "currentNode": "n1"
}
```

### 5.4 仿真上下文
```jsonc
{
  "processId": "proc_001",
  "currentNode": "n1",
  "currentRole": "applicant",
  "formData": { "bus_type": "", "order_count": 0, "total_price": 0, "manager_opinion": "" },
  "traceLog": [
    { "step": 1, "node": "n1", "role": "applicant", "action": "submit", "snapshot": {}, "time": 0 }
  ]
}
```

### 5.5 API 契约（6 个）
| API | 功能 |
|-----|------|
| `getTemplateFormList` | 表单模板列表 |
| `getFormFieldList` | 字段元数据池 |
| `getFormFieldValueList` | 仿真期字段值 + 权限矩阵 |
| `getMethodInfo` | 可绑定业务方法 |
| `doMethodProcess` | 业务事件执行 |
| `importProcessDefinition` | **新增**：从流程引擎导入流程定义 |

---

## 六、用户界面设计

### 6.1 流程列表页（主入口）
```
┌──────────────────────────────────────────────────────────────────┐
│ 🔍 搜索流程...        [+ 新建流程]   UI 框架: [Element Plus ▼]    │
├──────────────────────────────────────────────────────────────────┤
│ 流程名称        │ 表单模板        │ 状态     │ 操作                  │
│ 客车采购申请    │ 客车采购表 v1.0 │ 已导入   │ 设计│导入│仿真│导出  │
│ 售后维修派单    │ 维修派单表 v2.1 │ 草稿     │ 设计│导入│仿真│导出  │
│ 员工请假申请    │ 请假申请表 v1.2 │ 已导出   │ 设计│导入│仿真│导出  │
└──────────────────────────────────────────────────────────────────┘
```

### 6.2 行级操作交互
- 点击"设计表单"→ 跳转 `form-designer.html?processId=proc_001`，加载对应 Schema。
- 点击"导入流程"→ 弹窗显示流程定义 JSON 编辑器 + 节点-表单-角色配置表。
- 点击"仿真测试"→ 跳转 `process-simulation.html?processId=proc_001`。
- 点击"导出代码"→ 跳转 `code-export.html?processId=proc_001`。

### 6.3 设计器 / 仿真 / 导出布局
沿用 v1 的三栏 / 双栏布局，顶部控制栏增加"流程上下文"显示（当前流程名 + 返回列表）。

---

## 七、技术选型建议

| 层 | 选型 | 说明 |
|----|------|------|
| 前端框架 | Vue 3 (`<script setup>`) | 与导出目标一致 |
| 表单设计器 | **form-create** (FcDesigner) | 开源 MIT，多 UI 框架，Schema 驱动 |
| 表单运行时 | **TanStack Form** (`@tanstack/vue-form`) | headless，跨 UI 框架，类型安全 |
| Schema 适配 | 自研 Schema Adapter | form-create JSON ↔ TanStack Field |
| UI 库 | Element Plus（默认）/ Ant Design Vue / Naive UI | 可插拔 UI Adapter |
| 状态管理 | Pinia | 流程列表、编排 Schema、仿真上下文 |
| 拖拽 | form-create 内置 + Sortable.js | 设计器拖拽 |
| 持久化 | IndexedDB（localforage） | 流程列表、模板、测试记录 |
| 代码生成 | 字符串模板 + Prism.js 高亮 | 生成 SFC 并预览 |
| Mock | MSW / 本地 JSON | 6 个 API 契约 Mock |
| 流程引擎对接 | REST 适配器 | 导入流程定义 |

> 原型阶段使用纯 HTML + CSS + 原生 JS，无构建依赖，直观演示核心操作流程。

---

## 八、原型说明

原型位于 `prototype/` 目录，共 5 个 HTML 文件：

| 文件 | 对应模块 | 演示要点 |
|------|----------|----------|
| `index.html` | 导航入口 | 五大模块入口、整体概览 |
| `process-list.html` | **流程列表（主入口）** | 类 jeecg-boot 列表，行级四大能力入口、新建/搜索/状态标签、流程导入弹窗 |
| `form-designer.html` | 表单设计器 | 拖拽组件、属性配置、字段/动作/权限绑定、form-create/TanStack Form 适配说明 |
| `process-simulation.html` | 流程仿真测试 | 角色切换、流转、权限动态渲染、业务方法触发、异常注入、回放 |
| `code-export.html` | 代码导出 | 选项配置、SFC 实时预览（含 TanStack Form）、多 UI 库切换、下载 |

原型间通过 URL 参数 `?processId=proc_001` 传递流程上下文，共享同一套表单 Schema，保证演示连贯性。
