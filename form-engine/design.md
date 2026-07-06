
# 📑 表单引擎配置与测试工作台 —— AI 开发设计文档

## 1. 目标与概述

本项目的目标是构建一个**表单引擎配置与测试工作台**。该工作台独立于已有的流程引擎，专注于表单的编排、动态渲染、业务方法绑定、多角色全链路仿真测试，并最终支持导出可直接在 Vue 3 项目中运行的前端代码。

### 技术栈要求

* **前端框架**：Vue 3 (SFC, `<script setup>`)
* **状态管理/组件库**：根据通用规范（推荐 Element Plus 或 Ant Design Vue），保持响应式数据驱动。

---

## 2. 核心接口规范 (API Contracts)

AI 在实现页面时，需使用以下 5 个标准接口进行数据交互。请先在前端 mock 对应的数据结构：

### ① `getTemplateFormList`

* **功能**：获取表单模板列表
* **Mock 数据结构**：
```json
[
  { "templateId": "tmpl_001", "templateName": "客车采购申请表单", "version": "v1.0" },
  { "templateId": "tmpl_002", "templateName": "售后维修派单表", "version": "v2.1" }
]

```



### ② `getFormFieldList`

* **功能**：获取当前表单模板或底层数据表相关的标准字段列表（用于绑定画布组件）
* **Mock 数据结构**：
```json
[
  { "fieldKey": "bus_type", "fieldName": "客车型号", "fieldType": "String", "required": true },
  { "fieldKey": "order_count", "fieldName": "采购数量", "fieldType": "Integer", "required": true },
  { "fieldKey": "total_price", "fieldName": "总金额", "fieldType": "BigDecimal", "required": false },
  { "fieldKey": "manager_opinion", "fieldName": "经理审批意见", "fieldType": "String", "required": false }
]

```



### ③ `getFormFieldValueList`

* **功能**：获取特定实例/特定角色下的字段值列表与权限矩阵
* **Mock 数据结构**：
```json
{
  "values": { "bus_type": "纯电动大巴 KingLong-12", "order_count": 10, "total_price": 5000000 },
  "permissions": {
    "bus_type": "readonly",
    "order_count": "readonly",
    "total_price": "readonly",
    "manager_opinion": "write"
  }
}

```



### ④ `getMethodInfo`

* **功能**：获取可绑定的后端业务逻辑方法集合（用于表单事件联动）
* **Mock 数据结构**：
```json
[
  { "methodKey": "busService.calculateDiscount", "methodName": "计算大客户折扣率", "params": ["order_count"] },
  { "methodKey": "stockService.checkInventory", "methodName": "检查客车底盘库存", "params": ["bus_type", "order_count"] }
]

```



### ⑤ `doMethodProcess`

* **功能**：执行业务事件调用
* **入参**：`{ methodKey: string, params: Object }`
* **Mock 返回**：模拟执行成功，并能动态修改表单中的某个字段值（例如调用计算折扣后，自动更新 `total_price`）。

---

## 3. 页面布局与三大核心模式

UI 界面分为**顶部控制栏**与**主工作区**。主工作区由一个标签页（Tabs）切换三种模式：

### 🛠️ 模式一：表单设计与编排 (Design Mode)

* **左侧面板（组件库）**：提供基础输入框、数字框、下拉框、按钮等。
* **中间画布（Canvas）**：支持拖拽或点击进入画布。
* **右侧属性面板**：
* **字段绑定**：组件必须下拉绑定由 `getFormFieldList` 提供的 `fieldKey`。
* **动作绑定**：如果是按钮组件，支持在其点击事件上选择 `getMethodInfo` 里的业务方法。
* **权限矩阵配置**：可配置该组件在不同角色（如：申请人、经理）下的默认状态（可写/只读/隐藏）。



### 👥 模式二：多角色仿真测试沙箱 (Simulation Mode)

* **顶部工具栏**：
* 提供“角色切换”下拉框（模拟：`申请人` -> `部门经理` -> `财务总监`）。
* 提供“模拟流转/下一步”按钮。


* **沙箱运行逻辑**：
1. 当切换到 `申请人`：表单处于编辑状态，可填写“客车型号”、“采购数量”。点击画布绑定的“检查库存”按钮，触发 `doMethodProcess`。
2. 点击“模拟流转”：将当前前端表单数据暂存。
3. 切换到 `部门经理`：调用 `getFormFieldValueList`，刚才填写的型号和数量变为 `readonly`（只读），“经理审批意见”变为 `write`（可写）。



### 💾 模式三：代码生成与导出 (Export Mode)

* **预览区域**：实时展示基于当前编排 JSON 生成的 Vue 3 单文件组件（SFC）代码。
* **导出功能**：提供“下载 Vue 组件”按钮，导出的代码需包含完整的表单结构、`v-model` 绑定、以及预留的 `doMethodProcess` 业务事件占位函数。
