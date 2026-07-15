# 安保系统 BPM 流程集成架构方案

> **版本**：V1.0  
> **日期**：2026-07-06  
> **编制依据**：对 `safety-service` 后端代码和 `safety-web` 前端代码的全面分析

---

## 一、系统概述

### 1.1 技术栈

| 层级 | 技术选型 |
|------|---------|
| **前端** | Vue 3 + TypeScript + Vite + Ant Design Vue + Pinia |
| **后端** | Spring Boot 2.x + MyBatis + JDK 1.8 |
| **数据库** | MySQL 5.7+（主业务）+ SQL Server（门禁设备）+ PostgreSQL（海康设备） |
| **流程引擎** | 蓝凌 LBPM（Landray BPM） |
| **缓存** | Redis Cluster |
| **通信协议** | SOAP WebService（本系统与 BPM 集成） |

### 1.2 项目结构

```
safety-service/                          # 后端项目
├── safety-common/                       # 公共模块（常量、工具、VO）
├── safety-core/                         # 核心业务模块（Service/Handler/Entity）
│   └── service/bpm/                     # BPM 核心服务
├── safety-web/                          # Web 接口层（Controller/WebService）
└── safety-kafka/                        # Kafka 消息消费模块

safety-web/                              # 前端项目
├── src/api/                             # API 请求层
├── src/assets/bpm/                      # BPM 核心资产（domain.js 跨域通信库）
├── src/views/                           # 业务视图页面
│   ├── buildingAccessControl/           # 楼宇门禁管理
│   ├── gateAccessControl/               # 大门门禁管理
│   ├── exitpass/                        # 出门证管理
│   ├── registration/visitorApproval/    # 来访审批
│   ├── enterFiling/blackListEnterApply/ # 黑名单入场申请
│   └── patrol/                          # 巡检审批
└── .env.*                              # 环境配置（含 VITE_GLOB_BPM_URL）
```

---

## 二、业务流程全景

### 2.1 流程模块清单

| 流程名称 | 业务场景 | BPM ModelId | 表单ID | 状态码 |
|---------|---------|-------------|--------|--------|
| **楼宇门禁申请流程** | 员工申请楼宇门禁权限（开通/关闭） | `build` | `safety_build_process` | 10待提交, 20审批中, 30结束, 00废弃 |
| **楼宇门禁管理流程** | 门禁系统管理员/保密负责人调整责任部门 | `build_manage` | `safety_build_manage_process` | 同左 |
| **人车权限申请流程** | 员工申请厂区大门权限（人脸/卡/车） | `park` | `safety_park_power_process` | 10草稿, 20审批中, 30结束, 00废弃 |
| **物资出门证流程** | 货物出厂审批（含废料销售、物资出门） | `goods_out` | `safety_goods_out_process` | 20审批中, 30结束, 00废弃 |
| **出门证异常事件流程** | 出门证违规事件上报处理 | `goods_out_events` | `safety_goods_out_event_process` | 同左 |
| **设备点检发布流程** | 设备设施点检任务发布审批 | `spot_check` | `safety_spot_check_process` | 同左 |
| **监控岗发布流程** | 监控岗位任务发布审批 | `monitor_post` | `safety_monitor_post_process` | 同左 |
| **治安巡查发布流程** | 治安巡查任务发布审批 | `process_patrol` | `safety_process_patrol` | 同左 |
| **外协承诺变更流程** | 外来人员承诺信息变更 | `outsider_promise` | `safety_outsider_promise_change` | 同左 |
| **入场备案流程** | 黑名单人员特殊入厂备案 | `enter` | `safety_enter_filing` | 20待审核, 30通过, 40发布 |
| **黑名单入场申请流程** | 黑名单人员入厂申请 | `entry_blank` | `safety_entry_blank_process` | 20待审核, 30通过, 00废弃 |
| **访客授权审批流程** | 访客授权申请审批 | `visitor` | `safety_visitor_authorization_approval` | 20审批中, 30通过, 00废弃 |

### 2.2 流程状态码规范

| 状态码 | 含义 | 说明 |
|--------|------|------|
| `00` | 废弃 | 流程被创建人废弃 |
| `10` | 驳回/草稿 | 流程被驳回或初始草稿状态 |
| `20` | 审批中/待审核 | 流程正在审批或待审核 |
| `30` | 通过/结束 | 流程审批通过，业务生效 |
| `40` | 已发布 | 仅入场备案使用，权限已下发 |

---

## 三、BPM 集成架构

### 3.1 集成模式概述

系统采用**蓝凌 LBPM 流程引擎**，通过 `sysId = "SAFETY"` 作为异构系统标识，实现**双向通信**：

```
┌─────────────────────────────────────────────────────────────────┐
│                        宇通安保管理平台                            │
│                                                                 │
│  ┌──────────────┐    主动调用(SOAP)     ┌──────────────┐        │
│  │              │ ───────────────────→  │              │        │
│  │ safety-web   │                       │  蓝凌 BPM 引擎 │        │
│  │   Controller │ ←───────────────────  │   (Landray)   │        │
│  │              │    被动回调(WebService)│              │        │
│  └──────────────┘                       └──────────────┘        │
│         ↑                                    ↑                   │
│         │                                    │                   │
│         ▼                                    ▼                   │
│  ┌──────────────┐                       ┌──────────────┐        │
│  │  本系统数据   │    状态同步            │  BPM 流程    │        │
│  │  业务表      │ ←──────────────────→  │  实例/任务    │        │
│  └──────────────┘                       └──────────────┘        │
│                                                                 │
│  ┌──────────────┐                                               │
│  │ 前端 Vue 页面 │  iframe 嵌入 BPM 流程页面                      │
│  └──────────────┘  postMessage 跨域通信                          │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 双向通信机制

#### 主动调用（本系统 → BPM）

| 操作 | BPM SOAP 方法 | 调用地址 | 关键参数 |
|------|-------------|---------|---------|
| 创建流程 | `CreateProcess` | `bpm.flowWebService` | `flowTemplateId`, `formId`, `creator`, `docSubject` |
| 创建并提交（二合一） | `CreateAndApproveProcess` | `bpm.createAndApprove` | 同左，自动推进到下一节点 |
| 审批流程 | `ApproveProcess` | `bpm.flowWebService` | `formId`, `processId`, `handler`, `processParam` |
| 废弃流程 | `ApproveProcess` | `bpm.flowWebService` | `operationType=drafter_abandon` |
| 获取操作列表 | `GetOperationList` | `bpm.flowWebService` | `formId`, `processId` |
| 获取当前节点 | `GetCurrentNodesInfo` | `bpm.selfFlowInfo.url` | `processIds`（JSON 数组） |
| 获取审批人列表 | `GetApproverList` | `bpm.flowWebService` | `formId`, `processId`, `nodeId` |
| 传阅 | `passProcess` | `bpm.createAndApprove` | `flowId`, `fromUser`, `toUser` |

#### 被动回调（BPM → 本系统）

通过 JAX-WS 注解暴露 WebService 端点，BPM 在流程状态变更时回调：

```java
@WebService(serviceName = "safetyBpmWebService",
    targetNamespace = "http://com.yutong.safety.web.ws",
    endpointInterface = "com.yutong.safety.web.controller.web.webService.service.SafetyBpmWebService")
```

**六大回调接口**：

| 接口 | 功能 | 说明 |
|------|------|------|
| `getTemplateFormList` | 获取模板表单列表 | 查询 `safety_bpm_model` 表，返回所有流程模型 |
| `getFormFieldList` | 获取表单字段列表 | 查询数据库表结构（`information_schema.columns`） |
| `getFormFieldValueList` | 获取表单字段值 | 根据 `formInstanceId` 查询业务表数据 |
| `getMethodInfo` | 获取回调方法列表 | 查询 `safety_bpm_call_back` 表，返回注册的回调函数 |
| `doMethodProcess` | 执行回调函数 | 核心回调入口，根据 `functionId` 分发到不同处理器 |
| `synchronizeTemplate` | 模板同步 | 暂无使用 |

**回调函数映射表**（在 `doMethodProcess` 中 switch-case 分发）：

| functionId | 回调场景 | 处理器方法 | 核心操作 |
|------------|---------|-----------|---------|
| `safety_spot_check_publish` | 设备点检发布 | `updateFlowStatus` | 更新状态为 `RELEASE`，记录发布时间 |
| `safety_monitor_post_publish` | 监控岗发布 | `updateFlowStatus` | 同上 |
| `safety_process_patrol_publish` | 治安巡查发布 | `updateFlowStatus` | 更新节点为 `N3`（结束） |
| `safety_outsider_promise_change` | 外协承诺变更 | `updateFlowStatus` | 更新外协流程状态 |
| `safety_enter_filing_state_20/30/40` | 入场备案状态流转 | `updateEnterFilingState` | 更新备案状态（20待审/30通过/40发布） |
| `safety_enter_filing_pass` | 入场备案通过 | `callThirdPartyInterface` | 调用第三方接口下发权限 |
| `safety_goods_out_process` | 物资出门证发布 | `bpmCallBackGoodsOut` | 更新出门证状态，记录发布时间 |
| `safety_goods_out_events_process` | 出门证异常事件 | `bpmCallBackEventProcess` | 更新异常事件状态 |
| `safety_build_process_public` | 楼宇门禁公示发布 | `updateFlowStatus` | 更新流程状态 |
| `safety_build_process_enter` | 楼宇门禁入场回调 | `bpmCallBackProcess` | 记录入场信息 |
| `safety_build_process_discard` | 楼宇门禁废弃回调 | `discardProcess` | 更新流程状态为废弃 |
| `safety_build_process_after` | 楼宇门禁退场回调 | `afterProcess` | 记录退场信息 |
| `safety_park_power_process` | 人车权限发布 | `bpmCallBackProcess` | 更新当前节点，准备下发权限 |
| `safety_park_power_process_publish` | 人车权限下发 | `publishProcess` | **异步下发人脸/卡/车权限** |
| `safety_park_power_process_discard` | 人车权限废弃 | `discardProcess` | 更新流程状态为废弃 |
| `safety_entry_blank_process_pass` | 黑名单入场通过 | `updateFlowStatus` | 更新黑名单备案状态 |
| `safety_visitor_authorization_approval_pass` | 访客授权通过 | `updateFlowStatus` | 更新访客授权状态 |

**关键代码位置**：
- 回调实现：[SafetyBpmWebServiceImpl.java](file:///e:/workspaces/safety/safety-service/safety-web/src/main/java/com/yutong/safety/web/controller/web/webService/service/impl/SafetyBpmWebServiceImpl.java#L172-L214)
- BPM 模型配置表：`safety.safety_bpm_model`（存储 modelId、formId、formUrl）
- BPM 回调函数表：`safety.safety_bpm_call_back`（存储 functionId、functionName、functionDesc）

### 3.3 BPM 系统配置

#### 后端配置（application.properties）

| 配置项 | 测试环境 | 生产环境 |
|--------|---------|---------|
| `bpm.flowWebService` | `http://bpmtest.yutong.com/sys/webservice/flowWebService?wsdl` | `http://bpm.yutong.com/sys/webservice/flowWebService?wsdl` |
| `bpm.createAndApprove` | `http://bpmtest.yutong.com/sys/webservice/integratedWebExtendService?wsdl` | `http://bpm.yutong.com/sys/webservice/integratedWebExtendService?wsdl` |
| `bpm.selfFlowInfo.url` | `http://sguat.yutong.com/lbpm/selfFlowInfoService?apikey=...` | `http://sg.yutong.com/lbpm/selfFlowInfoService?apikey=...` |
| `bpm.backCall.formUrl` | `https://apitest.yutong.com:20443/static/examine-and-approve/#/auto-switch` | `https://mc.yutong.com/static/examine-and-approve/#/auto-switch` |

#### 前端配置（.env.*）

| 环境 | `VITE_GLOB_BPM_URL` |
|------|-------------------|
| development | `https://bpmtest.yutong.com/sys/lbpmdocking/lbpm_docking_main/lbpmDockingMain.do` |
| production | `https://bpm.yutong.com/sys/lbpmdocking/lbpm_docking_main/lbpmDockingMain.do` |

### 3.4 核心代码层级

```
业务 Controller/Handler
        ↓
SafetyBpmProcessService     # 流程操作业务封装层（组织参数、调用 SOAP、解析返回）
        ↓
BpmService                  # BPM SOAP 底层通信层（拼接 XML 报文、HttpWS.postSoap）
        ↓
蓝凌 BPM 引擎               # Landray BPM 流程引擎（流程实例/任务/审批）
        ↑
SafetyBpmWebServiceImpl     # BPM 回调入口（JAX-WS WebService 端点）
        ↓
各业务 Handler               # 回调分发后的业务处理（状态更新、权限下发等）
```

**关键类说明**：

| 类名 | 包路径 | 职责 |
|------|--------|------|
| `BpmService` | `core.service.bpm` | 底层 SOAP 通信，拼接 XML 报文调用 BPM WebService |
| `SafetyBpmProcessService` | `core.service` | 流程操作业务封装（创建/审批/驳回/废弃/传阅） |
| `SafetyBpmWebServiceService` | `core.service` | BPM 数据查询服务（模型/字段/回调函数） |
| `SafetyBpmWebServiceImpl` | `web.controller.web.webService.service.impl` | BPM 回调实现（6 大接口 + 回调分发） |
| `BpmProcessParam` | `core.db.entity.bpm` | 流程参数实体（formInstanceId、modelId、processId 等） |
| `SafetyBpmProcessHandler` | `core.handler` | 流程操作 Handler 封装（对 Service 的代理） |

---

## 四、核心流程详解

### 4.1 物资出门证流程

#### 4.1.1 业务场景

物资出门证用于货物出厂审批，涵盖废料销售、物资出门等场景。流程支持起草人提交后自动推进到第一个审批节点，各环节审批人可根据审批结果同意、驳回或部分放行。

**业务流程**：

```
起草人提交 → N2 废料销售审批 → N3 质保部审批 → N4 审核人上级 → N5 审核人 → N6 门卫放行 → N7 接收人确认 → 结束
```

| 节点 | 审批人 | 超时标记 | 关键逻辑 |
|------|--------|---------|---------|
| N2 | 废料销售审批人 | isTimeOut="2" | 常规审批 |
| N3 | 质保部审批人 | isTimeOut="3" | 常规审批 |
| N4 | 审核人上级 | isTimeOut="4" | 常规审批 |
| N5 | 审核人 | isTimeOut="5" | 检查历史异常记录 |
| N6 | 门卫放行 | isTimeOut="0" | 记录放行异常/抽查异常 |
| N7 | 接收人 | - | 更新接收状态，生成未接收异常记录 |

#### 4.1.2 前后端交互

**后端接口**：

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 保存并创建流程 | POST | `/web/safety/goodsOut/process/saveAndCreateProcess` | 保存数据 + 创建 BPM 流程（二合一） |
| 审批流程 | POST | `/web/safety/goodsOut/process/auditProcess` | 常规审批 |
| 审批流程（EIP） | POST | `/web/safety/goodsOut/process/auditForProcess` | 外部系统审批（参数合并） |
| 驳回流程 | POST | `/web/safety/goodsOut/process/rejectProcess` | 驳回到上一节点 |
| 获取当前审批人 | POST | `/web/safety/process/getProcessCurDealHandler` | 获取当前节点处理人 |
| 获取流程详情 | POST | `/web/safety/goodsOut/process/getDetailById` | 查询流程明细 |
| 更新审批人 | POST | `/web/safety/goodsOut/process/modifyCurDealHandler` | 更新流程处理人 |
| 转派 | POST | `/web/safety/goodsOut/process/updateTransfer` | 转派给其他处理人 |
| 传阅 | POST | `/web/safety/goodsOut/process/circulateProcess` | 传阅给其他用户 |

**关键参数**：

- **请求参数**：`GoodsOutProcessAddParam` / `GoodsOutProcessUpdateParam`
  - `outFactoryId`：出厂厂区
  - `qaExamineEmpNo`：质保部人员工号（可选）
  - `systemFlag`：来源系统（"4"=EIP）
  - `goodsList`：物资明细列表（JSON 数组）
  - `vehicleList`：车辆明细列表（JSON 数组）
  - `receiveList`：接收人列表（JSON 数组）
  - `nodeFlag`：节点标识（1~7）
  - `curPersonLogin`：当前处理人工号
  - `auditNote`：审批意见

- **返回结果**：
  - 创建流程：`"uuid,flowId"` 格式
  - 审批流程：`BpmNodeReturnVO`（status + message）

**关键代码位置**：
- Controller：[SafetyGoodsOutProcessController.java](file:///e:/workspaces/safety/safety-service/safety-web/src/main/java/com/yutong/safety/web/controller/safety/goodsOut/SafetyGoodsOutProcessController.java)
- Handler：[SafetyGoodsOutProcessHandler.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/handler/goodsOut/SafetyGoodsOutProcessHandler.java)
- Service：[SafetyGoodsOutProcessService.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/goodsOut/SafetyGoodsOutProcessService.java)

#### 4.1.3 数据流转与状态变更

```
┌─────────────────────────────────────────────────────────────────┐
│                      数据流转路径                                 │
│                                                                 │
│  前端表单 → 后端 Controller → Handler → Service → MySQL         │
│    ↓                                                              │
│  后端 Handler → SafetyBpmProcessService → BPM SOAP → 蓝凌 BPM   │
│    ↑                                                              │
│  蓝凌 BPM 回调 → SafetyBpmWebServiceImpl → 业务 Handler         │
│    ↓                                                              │
│  业务 Handler → Service.update() → MySQL 更新状态                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**状态变更规则**：

| 触发场景 | 本地表状态 | BPM 操作 | 说明 |
|---------|-----------|---------|------|
| 起草人提交 | 10 → 20 | `CreateAndApproveProcess` | 二合一，自动推进到 N2 |
| N2~N5 审批通过 | 保持 20 | `ApproveProcess`（operationType=handler_pass） | 推进到下一节点 |
| N6 门卫放行 | 保持 20 | `ApproveProcess` + 记录异常 | 记录放行异常/抽查异常 |
| N7 接收确认 | 保持 20 | `ApproveProcess` + 生成未接收记录 | 更新接收状态 |
| 任意节点驳回 | 保持 20 | `ApproveProcess`（operationType=handler_refuse） | 驳回到上一节点 |
| 流程发布 | 20 → 30 | 被动回调 `doMethodProcess` | BPM 回调更新状态 |
| 创建人废弃 | 20 → 00 | `ApproveProcess`（operationType=drafter_abandon） | 废弃流程 |

**关键字段**：
- `status`：流程状态（20/30/00）
- `bpm_flow_id`：BPM 流程实例 ID（`flowId`）
- `publish_time`：发布时间（状态变更为 30 时记录）
- `isTimeOut`：超时标记（不同节点不同值）

---

### 4.2 楼宇门禁申请流程

#### 4.2.1 业务场景

楼宇门禁申请流程用于员工申请楼宇门禁权限（开通或关闭）。流程支持**部分审批**（部分通过/部分不通过），适用于同时申请多个门禁权限的场景。

**角色类型**：

| 角色 | 含义 |
|------|------|
| "1" | 普通员工 |
| "2" | 门禁系统管理员 |
| "3" | 门禁管理员/保密负责人 |

**流程节点**：

```
申请人提交 → N2 管理员审批 → N5 本部门负责人审批 → N11 最终审批/废弃
```

| 节点 | 审批人 | 关键逻辑 |
|------|--------|---------|
| N2 | 门禁管理员 | 校验设备归属，判断是否涉及绝密/跨部门 |
| N5 | 本部门科长 | 部门审批 |
| N11 | 最终审批人 | 不通过则废弃流程 |

**特殊逻辑**：
- **白名单校验**：若关闭门禁且员工在白名单中，需发送通知
- **绝密区域判断**：涉及绝密区域（`topSecretFlag="1"`）需特殊审批
- **跨部门判断**：涉及跨部门（`acrossFlag="1"`）需额外审批
- **管理员审批**：按组织授权且组织数 ≤ 2 时，需门禁管理员审批（`adminApprovalStatus="1"`）

#### 4.2.2 前后端交互

**后端接口**：

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 保存并创建流程 | POST | `/web/build/safety/buildProcess/saveAndCreateProcess` | 保存 + 创建流程 |
| 审批流程 | POST | `/web/build/safety/buildProcess/auditProcess` | 常规审批 |
| 部分审批 | POST | `/web/build/safety/buildProcess/auditPartProcess` | 部分通过/部分不通过 |
| 获取当前审批人 | POST | `/web/build/safety/buildProcess/getProcessCurDealHandler` | 获取当前节点处理人 |
| 审批权限检查 | POST | `/web/build/safety/buildProcess/checkBuildRoleType` | 检查用户角色类型 |

**关键参数**：

- **请求参数**：`BuildProcessAddParam`
  - `createEmployeeLogin`：申请人工号
  - `powerType`：权限类型（"1"=开通, "2"=关闭, "1,2"=开通+关闭）
  - `openDOList`：开通明细列表
  - `closeDOList`：关闭明细列表
  - `roleType`：角色类型（1/2/3）

- **开通明细字段**：
  - `deviceId`：设备编码
  - `empowerType`：授权类型（"1"=员工授权）
  - `authStartTime`/`authEndTime`：授权起止时间
  - `acrossEmployeeName`：跨部门对接人

**关键代码位置**：
- Controller：[SafetyBuildProcessController.java](file:///e:/workspaces/safety/safety-service/safety-web/src/main/java/com/yutong/safety/web/controller/safety/build/SafetyBuildProcessController.java)
- Handler：[SafetyBuildProcessHandler.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/handler/build/SafetyBuildProcessHandler.java)
- Service：[SafetyBuildProcessService.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/build/SafetyBuildProcessService.java)

#### 4.2.3 数据流转

```
┌─────────────────────────────────────────────────────────────────┐
│                      数据流转路径                                 │
│                                                                 │
│  前端表单（含门禁列表）                                          │
│    ↓                                                            │
│  后端 Handler.saveAndCreateProcessByUserName()                  │
│    ├── 生成 UUID + 流程编号（LYMJSQ + 日期 + 序号）              │
│    ├── 用户类型校验（角色 1/2/3）                                │
│    ├── 白名单校验（关闭门禁时）                                   │
│    ├── Service.save() → 主表 + 开通明细表 + 关闭明细表           │
│    ├── Service.initSubmitParam() → 设置责任部门审批人            │
│    ├── handleOpenDeptInfo() → 判断绝密/跨部门                   │
│    └── SafetyBpmProcessService.createAndApproveProcess()       │
│         └── BPM CreateAndApproveProcess（二合一）               │
│                                                                 │
│  BPM 回调 → SafetyBuildProcessHandler.bpmCallBackProcess        │
│    └── 更新流程节点/状态                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

### 4.3 人车权限申请流程

#### 4.3.1 业务场景

人车权限申请流程用于员工申请厂区大门权限，审批通过后**自动下发**人脸、门禁卡、车辆权限到门禁设备系统。

**权限类型**：
- **人脸**：下发到人脸识别设备
- **门禁卡**：下发到门禁卡读写设备
- **车辆**：下发到车辆出入口系统

**流程节点**：

```
申请人提交 → N2 审批 → N7 科长审批 → N10 权限下发 → N12 园区权限负责人 → N3 结束
```

| 节点 | 审批人 | 关键逻辑 |
|------|--------|---------|
| N7 | 科长 | 常规审批 |
| N10 | 权限下发节点 | 触发异步权限下发 |
| N12 | 园区权限负责人 | 根据厂区编码 + 角色 30 动态判断 |

#### 4.3.2 权限下发核心逻辑

**关键字段**：
- `powerType`：权限类型（"1"=开通, "2"=关闭）
- `powerResult`：权限下发结果（"1"=成功, "0"=失败）

**权限下发链路**：

```
┌─────────────────────────────────────────────────────────────────┐
│                  权限下发核心链路                                  │
│                                                                 │
│  BPM 回调 → publishProcess()                                     │
│    ↓                                                            │
│  sendToCrk() [异步]                                              │
│    ↓                                                            │
│  downPower()                                                     │
│    ├── 查询变更前大门权限 → 保存到历史表                           │
│    ├── 查询员工车辆信息（未锁定）                                 │
│    ├── 逐人处理：                                                 │
│    │   ├── 开通：合并厂区（取合集），记录新权限                    │
│    │   └── 关闭：标记删除，同步关闭新开普/富士门禁                 │
│    ├── 过滤黑名单员工                                             │
│    └── powerUpdate() → 下发三类权限：                            │
│         ├── 人脸：获取员工照片 → IAM 人脸下发接口                  │
│         ├── 门禁卡：查询员工 Token → 按设备 GID 分组下发           │
│         └── 车辆：查询出入口系统 → 新增/更新进出厂记录             │
│                                                                 │
│  定时任务 updatePower() → 到期自动关闭权限                        │
│  定时任务 autoChangeFactoryAreaPower() → EHR 同步下发/注销        │
│  定时任务 autoCloseCarPower() → 到期自动关闭车辆权限               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**关键代码位置**：
- Handler：[SafetyParkPowerProcessHandler.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/handler/park/SafetyParkPowerProcessHandler.java)
  - `saveAndCreateProcess()`：第 145 行
  - `sendToCrk()`：第 1890 行
  - `downPower()`：第 1950 行
  - `powerUpdate()`：第 2100 行（下发人脸/卡/车）
  - `publishProcess()`：第 1800 行（权限下发）
- Service：[SafetyParkPowerProcessService.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/park/SafetyParkPowerProcessService.java)

#### 4.3.3 状态变更规则

| 触发场景 | 本地表状态 | 说明 |
|---------|-----------|------|
| 创建流程 | 10 → 20 | 草稿 → 审批中 |
| 审批通过 | 20 → 30 | 权限下发成功后 |
| BPM 回调（发布） | 20 → 30 | `safety_park_power_process_publish` 回调 |
| 废弃流程 | 20 → 00 | `powerResult=2`（不通过） |

---

### 4.4 黑名单入场申请流程

#### 4.4.1 业务场景

黑名单人员需特殊入厂时，通过此流程申请审批。审批通过后，状态流转为 20(待审核) → 30(通过) → 40(发布)，发布后调用第三方接口下发权限。

**关键代码位置**：
- Controller：[SafetyEntryBlankProcessController.java](file:///e:/workspaces/safety/safety-service/safety-web/src/main/java/com/yutong/safety/web/controller/safety/entry/SafetyEntryBlankProcessController.java)
- Handler：[SafetyEntryBlankProcessDetailHandler.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/handler/entry/SafetyEntryBlankProcessDetailHandler.java)

---

### 4.5 访客授权审批流程

#### 4.4.1 业务场景

访客授权申请用于外来人员入厂审批。审批通过后更新状态为 30（通过），并调用第三方接口。

**关键代码位置**：
- Handler：[SafetyVisitorAuthorizationApprovalHandler.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/handler/visitor/SafetyVisitorAuthorizationApprovalHandler.java)

---

## 五、前端 BPM 集成实现

### 5.1 跨域通信机制

由于 BPM 流程页面通过 iframe 嵌入，且部署在不同域名下，采用 **postMessage + document.domain** 实现跨域通信。

**核心文件**：
- `src/assets/bpm/domain.js`：跨域通信库
- `src/views/xxx/useBpm.ts`：各流程的 BPM Hook

**domain.js 核心方法**：

```javascript
// 注册可由外部调用的函数
domain.register(functionName, func)

// 向目标窗口发送消息（支持回调）
domain.call(win, fn, val, cb)

// 消息接收处理（解析 evt.data）
domain._receiver(evt)

// 自动调整 iframe 高度
domain.autoResize()
```

### 5.2 useBpm Hook

各流程页面通过 `useBpm.ts` 封装 BPM 相关逻辑：

**楼宇门禁版**（[useBpm.ts](file:///e:/workspaces/safety/safety-web/src/views/buildingAccessControl/accessRequestProcess/form/useBpm.ts)）：

```typescript
export function useBpm(props) {
  const { bpmUrl } = useGlobSetting();
  const route = useRoute();
  
  // 是否显示 BPM 流程图
  const isShowBpm = computed(() => !!route.query.bpmFlowId);
  
  // BPM iframe URL
  const iframeUrl = computed(() => 
    `${bpmUrl}?method=showProcess&formInstanceId=${route.query.formInstanceId}&processId=${route.query.bpmFlowId}`
  );
  
  // 获取 BPM 审批数据（通过 postMessage）
  const getBpmData = () => {
    return new Promise((resolve) => {
      window.domain.call(
        bpmIframe.value.contentWindow,
        'lbpm_getApprovalData',
        null,
        (res) => resolve(res)
      );
    });
  };
  
  // 检查当前用户是否可以审批
  const checkCanApprove = async (processId, status) => {
    if (status === '30' || status === '00') return false;
    const { data } = await getProcessCurDealHandler({ bpmFlowId: processId });
    const userStore = useUserStore();
    return data.some(node => node.fdCurrLoginNames?.includes(userStore.getUser.loginName));
  };
  
  return { isShowBpm, iframeUrl, getBpmData, checkCanApprove };
}
```

**黑名单版**（[useBpm.ts](file:///e:/workspaces/safety/safety-web/src/views/enterFiling/blackListEnterApply/useBpm.ts)）：

功能更丰富，额外包含：
- `isEdit`：编辑状态
- `isView`：查看模式
- `isApproveStatus`：是否为审批人
- `isRejectStatus`：是否被驳回
- `enableEdit()`：启用编辑模式

### 5.3 流程页面集成模式

**标准流程页面结构**：

```vue
<template>
  <!-- BPM 流程图 iframe -->
  <iframe v-if="isShowBpm" :src="iframeUrl" style="height: 1350px" />
  
  <!-- 业务表单 -->
  <BasicForm v-else ... />
  
  <!-- 审批按钮 -->
  <a-button v-if="isCanApprove" @click="auditProcessAction">审批</a-button>
</template>

<script setup lang="ts">
const { isShowBpm, iframeUrl, isCanApprove, getBpmData, checkCanApprove } = useBpm(props);

// 审批操作
const auditProcessAction = async () => {
  const bpmData = await getBpmData(); // 获取 BPM 审批数据
  await auditProcess({
    ...formData,
    fdParameterJson: bpmData.sysWfBusinessForm?.fdParameterJson
  });
};
</script>
```

### 5.4 Token 登录机制

**TokenLoginRoute**（[TokenLoginPage.vue](file:///e:/workspaces/safety/safety-web/src/views/sys/login/TokenLoginPage.vue)）：

用于 EIP/BPM 系统中的免密登录跳转：

```
1. EIP 系统携带 loginToken 跳转 → /tokenLogin?loginToken=xxx&info={taskId}
2. 前端调用 ThirdLogin({ token, thirdType: 'email' })
3. 登录成功后 → 跳转到 /task/handle/{taskId}
```

---

## 六、关键实现细节

### 6.1 流程创建（二合一）

**核心逻辑**：

```java
// SafetyGoodsOutProcessHandler.saveAndCreateProcessByUserName()
// 1. 生成 UUID 作为主键 ID
String id = UUID.randomUUID().toString();

// 2. 保存业务数据（状态 10/20）
safetyGoodsOutProcessService.save(dto);

// 3. 调用 BPM 创建并提交（二合一）
String result = safetyBpmProcessService.createAndApproveProcessByUserName(
    processSubject,        // 流程主题
    "goods_outsider",      // modelId
    "safety_goods_out_process",  // formId
    creatorUserName        // 创建人
);

// 4. 更新 bpm_flow_id
String[] parts = result.split(",");
String flowId = parts[1];
// 更新到本地表
```

**关键代码**：
- 后端：[SafetyBpmProcessService.createAndApproveProcessByUserName()](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/SafetyBpmProcessService.java)
- 前端：调用 `saveAndCreateProcess` API

### 6.2 审批操作

**核心逻辑**：

```java
// SafetyGoodsOutProcessHandler.updateAndAuditByUserName()
// 1. 根据 nodeFlag 更新业务数据
switch (nodeFlag) {
    case 1: isTimeOut = "2"; break;  // 废料销售审批
    case 3: isTimeOut = "3"; break;  // 质保部审批
    case 4: isTimeOut = "5"; break;  // 审核人审批
    // ...
}

// 2. 构建 BPM 审批参数
BpmProcessParam param = safetyBpmProcessService.initProcessParam(
    id,           // 流程实例 ID
    flowId,       // BPM 流程 ID
    modelId,      // 模型 ID
    formId,       // 表单 ID
    fdParameterJson, // BPM 表单参数 JSON
    userName,     // 审批人
    auditNote     // 审批意见
);

// 3. 调用 BPM 审批
BpmNodeReturnVO result = safetyBpmProcessService.approveProcessByUserName(param);
```

**关键代码**：
- 后端：[SafetyBpmProcessService.initProcessParam()](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/SafetyBpmProcessService.java)
- 前端：调用 `auditProcess` API + `getBpmData()` 获取 BPM 数据

### 6.3 流程回调

**核心逻辑**：

```java
// SafetyBpmWebServiceImpl.doMethodProcess()
@Override
public String doMethodProcess(String formId, String functionId, String processData, String language) {
    String functionIdValue = functionIdObj.getString("functionId");
    String processId = processObj.getString("processId");
    
    switch (functionIdValue) {
        case "safety_goods_out_process":
            safetyGoodsOutProcessHandler.bpmCallBackGoodsOut(processId, docStatus, processData);
            break;
        case "safety_park_power_process_publish":
            safetyParkPowerProcessHandler.publishProcess(processId, processData);
            break;
        // ... 其他回调
    }
    return "T";
}
```

**关键代码**：[SafetyBpmWebServiceImpl.doMethodProcess()](file:///e:/workspaces/safety/safety-service/safety-web/src/main/java/com/yutong/safety/web/controller/web/webService/service/impl/SafetyBpmWebServiceImpl.java#L172-L214)

### 6.4 BPM 参数组装

**`BpmProcessParam` 实体**（[BpmProcessParam.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/db/entity/bpm/BpmProcessParam.java)）：

| 字段 | 说明 |
|------|------|
| `formInstanceId` | 表单实例 ID（业务主键） |
| `modelId` | 流程模型 ID（如 `goods_out`） |
| `templateFormId` | 模板表单 ID（如 `safety_goods_out_process`） |
| `processId` | BPM 流程 ID（`flowId`） |
| `bpmTemplateId` | BPM 模板 ID |
| `processParam` | 流程参数 JSON |
| `subject` | 流程主题 |
| `userName` | 用户名 |

### 6.5 SOAP 调用

**BpmService.createBpmFlow()**（[BpmService.java](file:///e:/workspaces/safety/safety-service/safety-core/src/main/java/com/yutong/safety/core/service/bpm/BpmService.java)）：

```java
// 拼接 SOAP XML 报文
String xmlStr = "<soapenv:Envelope>..." +
    "<flowTemplateId>" + processParam.getBpmTemplateId() + "</flowTemplateId>" +
    "<formId>{\"sysId\":\"SAFETY\",\"modelId\":\"" + modelId + "\"}</formId>" +
    "<creator>{\"LoginName\":\"" + userName + "\"}</creator>" +
    "</soapenv:Envelope>";

// 调用 BPM WebService
String result = HttpWS.postSoap(bpmUrl, xmlStr);

// 解析返回结果
if (result.startsWith("T:")) {
    String flowId = result.substring(2);
    return flowId;  // 创建成功
}
```

---

## 七、数据表关系

### 7.1 核心业务表

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `safety_build_process` | 楼宇门禁流程主表 | `id`, `bpm_flow_id`, `status`, `power_type` |
| `safety_build_process_open` | 楼宇门禁开通明细 | `id`, `apply_status`, `device_id` |
| `safety_build_process_close` | 楼宇门禁关闭明细 | `id`, `apply_status` |
| `safety_goods_out_process` | 物资出门证主表 | `id`, `bpm_flow_id`, `status`, `is_time_out` |
| `safety_goods_out_process_goods` | 出门证物资明细 | `id`, `process_id` |
| `safety_park_power_process` | 人车权限主表 | `id`, `bpm_flow_id`, `power_result` |
| `safety_park_power_process_detail` | 人车权限明细 | `id`, `process_id`, `power_type` |
| `safety_entry_blank_process` | 黑名单入场备案 | `id`, `bpm_flow_id`, `status` |
| `safety_visitor_authorization_approval` | 访客授权审批 | `id`, `bpm_flow_id`, `status` |

### 7.2 BPM 配置表

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `safety_bpm_model` | BPM 模型配置 | `model_id`, `form_id`, `form_name`, `form_url` |
| `safety_bpm_call_back` | BPM 回调函数配置 | `model_id`, `function_id`, `function_name`, `function_desc` |

---

## 八、附录

### 8.1 关键代码文件索引

| 模块 | 文件路径 | 说明 |
|------|---------|------|
| **BPM 核心 SOAP** | `safety-core/service/bpm/BpmService.java` | 底层 SOAP 通信 |
| **BPM 业务封装** | `safety-core/service/SafetyBpmProcessService.java` | 流程操作封装 |
| **BPM 回调实现** | `safety-web/webService/service/impl/SafetyBpmWebServiceImpl.java` | 回调入口 + 分发 |
| **BPM 数据查询** | `safety-core/service/SafetyBpmWebServiceService.java` | 模型/字段查询 |
| **BPM 实体** | `safety-core/db/entity/bpm/BpmProcessParam.java` | 流程参数实体 |
| **BPM Mapper** | `safety-core/db/mapper/SafetyBpmWebServiceMapper.xml` | BPM 配置表映射 |
| **出门证 Handler** | `safety-core/handler/goodsOut/SafetyGoodsOutProcessHandler.java` | 出门证核心逻辑 |
| **门禁 Handler** | `safety-core/handler/build/SafetyBuildProcessHandler.java` | 门禁核心逻辑 |
| **人车权限 Handler** | `safety-core/handler/park/SafetyParkPowerProcessHandler.java` | 人车权限 + 下发 |
| **前端 domain.js** | `safety-web/src/assets/bpm/domain.js` | 跨域通信库 |
| **前端 useBpm** | `safety-web/src/views/*/useBpm.ts` | 各流程 BPM Hook |
| **环境配置** | `safety-web/.env.development` / `.env.production` | BPM URL 配置 |
| **后端配置** | `safety-web/src/main/profiles/*/application.properties` | BPM SOAP 地址配置 |

### 8.2 术语表

| 术语 | 说明 |
|------|------|
| BPM | 蓝凌 LBPM 流程引擎 |
| sysId | 系统标识，本系统为 "SAFETY" |
| modelId | 流程模型 ID（如 `goods_out`、`build`） |
| formId | 表单 ID（如 `safety_goods_out_process`） |
| formInstanceId | 表单实例 ID（业务数据主键） |
| flowId | BPM 流程实例 ID（回调时返回） |
| bpm_flow_id | 本地业务表中的 BPM 流程 ID 字段 |
| postMessage | 浏览器跨域通信 API |
| SOAP | Simple Object Access Protocol，XML 协议 |
| WebService | JAX-WS 暴露的 SOAP 服务 |

---

> **文档说明**：本文档基于对 `safety-service` 和 `safety-web` 项目的全面代码分析整理，重点梳理了流程类功能的完整运行交互逻辑。如需了解特定流程的更多细节，请参考文档中标注的关键代码位置。
