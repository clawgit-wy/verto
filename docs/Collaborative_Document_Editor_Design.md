# 协同文档编辑系统设计方案

> 基于 JeecgBoot 3.9.2 AI 简历功能扩展，实现协同编辑、批注评论、权限管理、文档模版等能力

---

## 1. 方案背景与目标

### 1.1 现状分析

JeecgBoot 3.9.2 内置了基于 `canvas-editor` 的 AI 简历功能（`airag/wordtpl` 模块），具备在线文档编辑和 Word 导出能力。但当前功能存在以下局限：

| 局限 | 说明 |
|------|------|
| 只有模版，没有文档 | 当前只管理"模版"（`aigc_word_template`），没有"基于模版创建的文档"概念 |
| 无协同编辑 | canvas-editor 是纯前端单实例编辑器，无 OT/CRDT 协同能力，多人同时编辑会导致覆盖丢失 |
| 无权限体系 | 权限注解被注释掉，所有人都能操作所有模版 |
| 无文档分类 | 没有文件夹/分类，所有模版平铺 |
| 模版与文档混在一起 | 模版（可复用的骨架）和文档（具体实例）共用一张表，无法区分 |
| 无批注功能 | 不支持类似 Word 批注的选中文字评论能力 |

### 1.2 扩展目标

基于现有 `canvas-editor` + POI/poi-tl 技术栈，扩展为完整的协同文档编辑系统，解决公司内部文档系统老旧、仅支持 Word 格式的痛点：

| 目标维度 | 量化指标 |
|---------|---------|
| 文档在线化 | 公司常用文档类型 80%+ 可在线编辑并导出 Word |
| 协同效率 | 文档评审周期缩短 50%（通过批注+通知替代线下传文件） |
| 权限管控 | 私有文档 100% 受权限保护，杜绝越权访问 |
| 模版复用 | 常用文档模版覆盖率 > 80%，新人 5 分钟内可创建标准文档 |

---

## 2. 现有能力分析

### 2.1 JeecgBoot AI 简历功能现有技术栈

| 能力 | 实现方式 | 关键文件 |
|------|---------|---------|
| 在线文档编辑 | canvas-editor（Canvas 渲染富文本编辑器） | `src/components/wordtpl/DocDesign.ts` |
| Word 导出 | POI (`XWPFDocument`) + poi-tl (`XWPFTemplate.render`) | `WordTplUtils.java` |
| Word 导入解析 | 解析 .docx → 还原为 canvas-editor 的 JSON 数据结构 | `WordTplUtils.parseWordFile()` |
| 模版变量填充 | `{{变量名}}` 语法 + poi-tl 渲染 | `WordTplGenDTO.java` |
| AI 生成简历 | 调用 AI 流程 (`airag/flow/run`)，AI 自动填充变量并创建模版记录 | `TestAiGenWordEnhance.java` |
| 纸张/页边距/水印 | 通过编辑器 UI 配置，存入 `aigc_word_template` 表 | `EoaWordTemplate.java` |

### 2.2 canvas-editor 关键 API（批注功能实现基础）

| API | 能力 | 用途 |
|-----|------|------|
| `instance.getRange()` | 获取当前选区 `{startIndex, endIndex}` | 定位批注关联的文本范围 |
| `instance.getSelection()` | 获取选中的元素列表 | 获取被批注的文本内容 |
| `instance.getElementList()` | 获取全部元素列表 | 全文检索与定位 |
| `instance.register.getContextMenuList()` | 自定义右键菜单 | 添加"添加批注"菜单项 |
| `instance.listener.rangeStyleChange` | 选区变化监听 | 点击批注高亮区域时联动右侧面板 |
| `EditorComponent.COMMENT` | 内置 comment 组件类型 | 渲染批注侧边栏 |
| `element.groupIds` | 元素分组标记字段 | 给被批注的文本打上组标记并高亮渲染 |

### 2.3 现有数据模型

```sql
-- 当前表结构：aigc_word_template（纯模版，无文档/权限概念）
CREATE TABLE aigc_word_template (
  id varchar(36) PRIMARY KEY,
  create_by varchar(50),
  create_time datetime,
  update_by varchar(50),
  update_time datetime,
  sys_org_code varchar(64),
  name varchar(32),
  code varchar(32),
  header text,
  footer text,
  main text,
  margins varchar(200),
  width int, height int,
  paper_direction varchar(32),
  watermark varchar(200)
);
```

---

## 3. 数据模型设计

### 3.1 文档实例表（新增：基于模版创建的文档）

```sql
CREATE TABLE `fe_document` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `template_id` varchar(36) DEFAULT NULL COMMENT '来源模版ID',
  `folder_id` varchar(36) DEFAULT NULL COMMENT '所属文件夹ID',
  `doc_type` varchar(20) DEFAULT 'word' COMMENT '文档类型: word/excel/ppt',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态: draft=草稿, reviewing=审核中, published=已发布, archived=已归档',
  `visibility` varchar(20) DEFAULT 'private' COMMENT '可见性: private=仅自己可见, shared=分享可见, public=全员可见',
  `version` int DEFAULT 1 COMMENT '版本号',
  `header` text COMMENT '页眉JSON',
  `footer` text COMMENT '页脚JSON',
  `main` longtext COMMENT '主体内容JSON',
  `margins` varchar(200) DEFAULT NULL COMMENT '页边距',
  `width` int DEFAULT 794 COMMENT '宽度',
  `height` int DEFAULT 1123 COMMENT '高度',
  `paper_direction` varchar(32) DEFAULT 'vertical' COMMENT '纸张方向',
  `watermark` varchar(200) DEFAULT NULL COMMENT '水印',
  `word_file` varchar(500) DEFAULT NULL COMMENT 'Word文件存储路径(生成的docx)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `sys_org_code` varchar(64) DEFAULT NULL COMMENT '所属部门',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档实例表';
```

### 3.2 文档权限表（新增：细粒度权限控制）

```sql
CREATE TABLE `fe_document_permission` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `document_id` varchar(36) NOT NULL COMMENT '文档ID',
  `target_type` varchar(20) NOT NULL COMMENT '授权对象类型: user=用户, role=角色, dept=部门, link=链接分享',
  `target_id` varchar(36) DEFAULT NULL COMMENT '授权对象ID(link类型时为空)',
  `permission` varchar(20) NOT NULL COMMENT '权限: view=查看, edit=编辑, manage=管理, export=导出',
  `grant_by` varchar(50) DEFAULT NULL COMMENT '授权人(分享人)',
  `grant_time` datetime DEFAULT NULL COMMENT '授权时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间(可选, null表示永不过期)',
  `share_link` varchar(500) DEFAULT NULL COMMENT '分享链接(token, 仅link类型)',
  `share_link_password` varchar(50) DEFAULT NULL COMMENT '分享链接密码(可选)',
  `share_link_expire` datetime DEFAULT NULL COMMENT '分享链接过期时间',
  `notify_target` tinyint(1) DEFAULT 1 COMMENT '是否通知被分享人: 0否 1是',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_perm` (`document_id`, `target_type`, `target_id`, `permission`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_share_link` (`share_link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档权限表';
```

### 3.3 文档文件夹表（新增：文档分类管理）

```sql
CREATE TABLE `fe_document_folder` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `parent_id` varchar(36) DEFAULT '0' COMMENT '父级ID',
  `folder_name` varchar(100) NOT NULL COMMENT '文件夹名称',
  `folder_path` varchar(500) DEFAULT NULL COMMENT '文件夹路径(冗余, 如:/公司文档/技术文档)',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_no` decimal(8,2) DEFAULT 0 COMMENT '排序号',
  `is_public` tinyint(1) DEFAULT 0 COMMENT '是否公开: 0否 1是',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `sys_org_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档文件夹表';
```

### 3.4 文档编辑锁表（新增：协同编辑互斥控制）

```sql
CREATE TABLE `fe_document_lock` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `document_id` varchar(36) NOT NULL COMMENT '文档ID',
  `user_id` varchar(36) NOT NULL COMMENT '持锁用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '持锁用户姓名',
  `lock_time` datetime NOT NULL COMMENT '加锁时间',
  `last_heartbeat` datetime NOT NULL COMMENT '最后心跳时间',
  `expire_time` datetime DEFAULT NULL COMMENT '锁过期时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_id` (`document_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_heartbeat` (`last_heartbeat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档编辑锁表';
```

### 3.5 文档版本历史表（新增：版本追溯）

```sql
CREATE TABLE `fe_document_version` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `document_id` varchar(36) NOT NULL COMMENT '文档ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `title` varchar(200) DEFAULT NULL COMMENT '文档标题',
  `main` longtext COMMENT '主体内容JSON快照',
  `header` text COMMENT '页眉快照',
  `footer` text COMMENT '页脚快照',
  `change_summary` varchar(500) DEFAULT NULL COMMENT '变更说明',
  `word_file` varchar(500) DEFAULT NULL COMMENT '该版本Word文件路径',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_version` (`document_id`, `version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本历史表';
```

### 3.6 文档批注表（新增：类似 Word 批注的评论功能）

```sql
CREATE TABLE `fe_document_comment` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `document_id` varchar(36) NOT NULL COMMENT '文档ID',
  `parent_id` varchar(36) DEFAULT NULL COMMENT '父评论ID(回复时非空)',
  `root_id` varchar(36) DEFAULT NULL COMMENT '根评论ID(同一批注线索)',
  `start_index` int NOT NULL COMMENT '选区起始元素索引(对应canvas-editor的startIndex)',
  `end_index` int NOT NULL COMMENT '选区结束元素索引(对应canvas-editor的endIndex)',
  `selected_text` varchar(2000) DEFAULT NULL COMMENT '被批注的原文内容(冗余存储，防止文档修改后丢失)',
  `group_id` varchar(36) DEFAULT NULL COMMENT '批注分组ID(同一段文本的多个批注共享)',
  `content` text NOT NULL COMMENT '批注内容',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active=活跃, resolved=已解决, dismissed=已忽略',
  `comment_type` varchar(20) DEFAULT 'comment' COMMENT '类型: comment=批注, reply=回复, system=系统消息',
  `create_by` varchar(50) DEFAULT NULL COMMENT '评论人',
  `create_time` datetime DEFAULT NULL COMMENT '评论时间',
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `resolved_by` varchar(50) DEFAULT NULL COMMENT '解决人',
  `resolved_time` datetime DEFAULT NULL COMMENT '解决时间',
  PRIMARY KEY (`id`),
  KEY `idx_document_id` (`document_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_start_end` (`document_id`, `start_index`, `end_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档批注表';
```

### 3.7 模版分类表（新增：模版市场分类）

```sql
CREATE TABLE `fe_template_category` (
  `id` varchar(36) NOT NULL COMMENT '主键ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_no` decimal(8,2) DEFAULT 0,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模版分类表';
```

### 3.8 现有模版表扩展（在 `aigc_word_template` 上增加字段）

```sql
ALTER TABLE aigc_word_template
  ADD COLUMN `category_id` varchar(36) DEFAULT NULL COMMENT '模版分类ID',
  ADD COLUMN `category` varchar(20) DEFAULT 'general' COMMENT '模版分类: general=通用, hr=人事, finance=财务, tech=技术, meeting=会议',
  ADD COLUMN `tags` json DEFAULT NULL COMMENT '标签(JSON数组)',
  ADD COLUMN `is_system` tinyint(1) DEFAULT 0 COMMENT '是否系统模版: 0否 1是(系统模版不可删除)',
  ADD COLUMN `description` varchar(500) DEFAULT NULL COMMENT '模版描述',
  ADD COLUMN `preview_image` varchar(500) DEFAULT NULL COMMENT '预览图URL',
  ADD COLUMN `use_count` int DEFAULT 0 COMMENT '使用次数',
  ADD COLUMN `del_flag` char(1) DEFAULT '0' COMMENT '删除标记';
```

### 3.9 ER 关系图

```
┌──────────────────┐       ┌──────────────────┐
│ fe_template_     │       │ aigc_word_       │
│ category         │──1:N──│ template (模版)   │
│ (模版分类)       │       │ +category,tags   │
└──────────────────┘       │ +is_system,desc  │
                           └───────┬──────────┘
                                   │ template_id (可选)
                                   ▼
┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ fe_document_     │──1:N──│ fe_document      │──1:N──│ fe_document_     │
│ folder (文件夹)  │       │ (文档实例)       │       │ version (版本)   │
└──────────────────┘       │                  │       └──────────────────┘
                           │ title,status     │
                           │ main,header...   │       ┌──────────────────┐
                           │ word_file        │──1:1──│ fe_document_     │
                           └───────┬──────────┘       │ lock (编辑锁)    │
                                   │                  └──────────────────┘
                                   │ document_id
                                   ▼
                           ┌──────────────────┐
                           │ fe_document_     │
                           │ comment (批注)   │
                           │ 选区定位+评论    │
                           │ 回复+解决        │
                           └───────┬──────────┘
                                   │ document_id
                                   ▼
                           ┌──────────────────┐
                           │ fe_document_     │
                           │ permission (权限) │
                           │ user/role/dept   │
                           │ view/edit/manage │
                           └──────────────────┘
```

---

## 4. 协同编辑方案设计（方案 D：编辑锁 + 实时通知）

### 4.1 方案选型

| 方案 | 协同能力 | 与现有系统兼容性 | 工作量 | 推荐度 |
|------|---------|----------------|--------|--------|
| A. 替换为协同编辑器 | 原生协同 | 需重写编辑器+导出 | 🔴 极大 | ❌ |
| B. canvas-editor + Y.js CRDT | 实时协同 | 需开发 Y.js Binding | 🟡 大 | ⚠️ |
| C. 乐观锁 + 手动合并 | 非实时协同 | 兼容现有代码 | 🟢 中 | ⚠️ |
| **D. 编辑锁 + 实时通知** | **互斥协同** | **兼容现有代码** | **🟢 小** | **✅ 推荐** |

**选择方案 D 的核心原因**：

- 公司内部文档场景以"个人撰写 + 评审修改"为主，不需要 Google Docs 级别的实时多人同时编辑
- canvas-editor 原生不支持协同，改造为 CRDT 成本极高
- 编辑锁实现简单：同一文档同一时间只允许一人编辑，其他人查看
- 后续如果需要真正的实时协同，可通过增量操作同步逐步演进

### 4.2 编辑锁机制

```
┌────────────────────────────────────────────────────────┐
│                   编辑锁生命周期                         │
│                                                         │
│  1. 用户打开文档编辑 → POST /acquireLock               │
│     ├── 无锁 → 返回 lockInfo, 创建锁记录               │
│     ├── 已有锁(自己) → 更新心跳, 返回 lockInfo         │
│     └── 已有锁(他人) → 返回持锁人信息, 前端提示        │
│                                                         │
│  2. 编辑中 → POST /heartbeat (每15秒)                  │
│     └── 更新 last_heartbeat 时间戳                     │
│                                                         │
│  3. 定时任务(每30秒) → 检查过期锁                      │
│     └── last_heartbeat < NOW()-30s → 自动释放 + 通知   │
│                                                         │
│  4. 用户关闭编辑器 → POST /releaseLock                 │
│     └── 删除锁记录 + WebSocket 广播                    │
│                                                         │
│  5. 强制接管 → POST /forceReleaseLock                  │
│     └── 校验权限 → 删除旧锁 → 创建新锁                 │
└────────────────────────────────────────────────────────┘
```

### 4.3 WebSocket 协同通知

**端点定义**：

```java
@ServerEndpoint("/ws/document/collab/{documentId}/{token}")
@Component
public class DocumentCollabWebSocket {
    @OnOpen    // 用户连接 → 广播"用户XXX进入文档"
    @OnMessage // 接收消息: CURSOR_MOVE / SELECTION_CHANGE / CONTENT_CHANGE / USER_LEAVE
    @OnClose   // 用户断开 → 释放锁 + 广播"用户XXX离开文档"
}
```

**消息格式**：

```json
{
  "type": "CURSOR_MOVE | SELECTION_CHANGE | CONTENT_CHANGE | USER_JOIN | USER_LEAVE | LOCK_ACQUIRED | LOCK_RELEASED | COMMENT_ADDED | COMMENT_RESOLVED",
  "documentId": "xxx",
  "userId": "xxx",
  "userName": "张三",
  "data": {},
  "timestamp": 1718000000000
}
```

### 4.4 协同编辑交互流程

```
用户A打开文档 → 获取编辑锁 → WebSocket通知其他在线用户
       │                    → 其他用户显示"只读 + 正在编辑人信息"
       │
       ├── 用户A编辑中 → 每N秒自动保存 → 广播文档变更通知
       │
       ├── 用户B请求编辑 → 提示"用户A正在编辑"
       │       ├── 等待（加入等待队列）
       │       └── 强制接管（需manage权限）
       │
       ├── 用户A关闭/离开 → 释放编辑锁 → 通知等待队列
       │
       └── 异常断开 → 心跳超时(30s) → 自动释放锁
```

---

## 5. 批注（评论）功能设计

### 5.1 功能描述

支持用户选中文档中的文字，添加批注评论，类似 Word 批注功能。支持回复、解决、高亮定位。

### 5.2 交互界面

```
┌─────────────────────────────────────────────────────────────────────┐
│                        文档编辑界面                                   │
│                                                                      │
│  ┌────────────────────────────────┐  ┌──────────────────────────┐   │
│  │                                │  │  批注面板（右侧）         │   │
│  │   正文内容...                  │  │                          │   │
│  │                                │  │  ┌──────────────────┐   │   │
│  │   这是一段[被批注的文本]①      │  │  │ ① 张三 10:30      │   │   │
│  │          ↑ 高亮+上标序号       │  │  │ 这句话需要修改      │   │   │
│  │                                │  │  │                    │   │   │
│  │   继续的正文内容...            │  │  │ 💬 李四 11:20      │   │   │
│  │                                │  │  │ 同意，建议改为...   │   │   │
│  │   另一段[批注文本]②           │  │  │                    │   │   │
│  │                                │  │  │ [回复] [解决]       │   │   │
│  │                                │  │  └──────────────────┘   │   │
│  │                                │  │                          │   │
│  │                                │  │  ┌──────────────────┐   │   │
│  │                                │  │  │ ② 王五 14:00      │   │   │
│  │                                │  │  │ 此处数据需要核实   │   │   │
│  │                                │  │  │ [回复] [解决]       │   │   │
│  │                                │  │  └──────────────────┘   │   │
│  └────────────────────────────────┘  └──────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.3 批注交互流程

```
1. 用户选中文本 → 右键菜单出现"添加批注"
                    │
                    ▼
2. 右侧批注面板弹出输入框 → 输入批注内容 → 点击"发送"
                    │
                    ▼
3. 被批注文本高亮显示 + 右上角出现批注序号①
                    │
                    ▼
4. 其他人看到 → 点击高亮文本 → 右侧面板定位到该批注 → 可回复
                    │
                    ▼
5. 批注解决 → 标记为"已解决" → 高亮变灰 → 折叠到已解决区
```

### 5.4 批注与编辑锁的关系

**关键设计点：批注不需要编辑锁**。批注不修改文档正文内容，只是在上层叠加评论：

| 场景 | 编辑锁 | 批注权限 | 说明 |
|------|--------|---------|------|
| 文档所有者 | 可编辑 | 可添加/回复/解决 | 完全控制 |
| 有 edit 权限的人 | 可编辑 | 可添加/回复/解决 | 同上 |
| 有 view 权限的人 | 只读 | 可添加/回复 | **只读不等于不能批注** |
| 无权限的人 | 不可见 | 不可见 | — |

因此：
- 张三在编辑正文（持有编辑锁）
- 李四同时可以在旁边加批注（不需要编辑锁）
- 两人互不冲突

### 5.5 批注高亮渲染实现

基于 canvas-editor 的 `groupIds` 字段实现批注高亮：

```typescript
function applyCommentHighlight(groupId: string, startIndex: number, endIndex: number) {
  const elementList = instance.getElementList();
  for (let i = startIndex + 1; i <= endIndex; i++) {
    const element = elementList[i];
    if (!element.groupIds) element.groupIds = [];
    if (!element.groupIds.includes(groupId)) element.groupIds.push(groupId);
    element.highlight = '#fff3cd';
  }
  instance.render({ isSetCursor: false });
}

function removeCommentHighlight(groupId: string, startIndex: number, endIndex: number) {
  const elementList = instance.getElementList();
  for (let i = startIndex + 1; i <= endIndex; i++) {
    const element = elementList[i];
    if (element.groupIds) {
      element.groupIds = element.groupIds.filter(id => id !== groupId);
      if (element.groupIds.length === 0) {
        delete element.groupIds;
        delete element.highlight;
      }
    }
  }
  instance.render({ isSetCursor: false });
}
```

### 5.6 右键菜单"添加批注"注册

在 `DocDesign.ts` 的 `createEditor` 方法中扩展：

```typescript
const contextMenuList = await instance.register.getContextMenuList();
contextMenuList.push({
  key: 'addComment',
  name: '添加批注',
  icon: 'comment',
  when: () => {
    const range = instance.command.getRange();
    return range.startIndex !== range.endIndex;
  },
  callback: () => {
    const rangeManager = instance.getRange();
    const { startIndex, endIndex } = rangeManager.getRange();
    const selection = instance.getSelection();
    const selectedText = selection
      ? selection.map(el => el.value).join('')
      : '';
    emit('addComment', {
      startIndex,
      endIndex,
      selectedText,
      groupId: generateUUID()
    });
  }
});
```

### 5.7 编辑器与批注面板联动

```typescript
// 监听选区变化 → 检查是否点击了批注高亮区域
instance.listener.rangeStyleChange = function(payload) {
  if (payload.groupIds && payload.groupIds.length > 0) {
    emit('commentFocus', payload.groupIds);
  }
};

// 点击批注面板 → 编辑器定位到对应文本
function scrollToPosition(startIndex: number, endIndex: number) {
  instance.command.executeSetRange(startIndex, endIndex);
  const cursorPosition = instance.getPosition().getCursorPosition();
  if (cursorPosition) {
    const scrollContainer = document.querySelector('.editor-container');
    if (scrollContainer) {
      scrollContainer.scrollTo({
        top: cursorPosition.coordinate?.leftTop?.[1] - 100,
        behavior: 'smooth'
      });
    }
  }
}
```

### 5.8 批注导出到 Word

POI 原生支持 Word 批注，导出时将在线批注转为 Word 批注格式：

```java
private void renderComments(XWPFDocument doc, List<FeDocumentComment> comments) {
    for (FeDocumentComment comment : comments) {
        if (comment.getParentId() != null) continue;
        XWPFComment wordComment = doc.createComment(comment.getId());
        XWPFParagraph commentPara = wordComment.createParagraph();
        XWPFRun commentRun = commentPara.createRun();
        commentRun.setText(comment.getCreateBy() + ": " + comment.getContent());
        addCommentReference(doc, comment.getStartIndex(),
            comment.getEndIndex(), comment.getId());
    }
}
```

---

## 5.9 AI 自动生成文档内容

### 5.9.1 现有 AI 简历生成机制分析

JeecgBoot 3.9.2 的 AI 简历功能已具备完整的"AI 生成文档内容"链路，核心机制如下：

**生成流程**：

```
用户输入基础信息 → AI流程编排(airag/flow/run) → 大模型生成JSON → Java增强节点保存为模版
```

**核心代码链路**：

1. **前端调用**：`EoaWordTemplateList.vue` 中 `generateResume()` 方法调用 `/airag/flow/run` 接口
2. **AI 流程执行**：`AiragChatServiceImpl.sendWithFlow()` 运行指定 flowId 的 AI 流程
3. **大模型生成**：AI 流程中的 LLM 节点根据 Prompt 生成内容，输出 canvas-editor 的 JSON 格式
4. **Java 增强节点**：`TestAiGenWordEnhance.java` 实现 `IAiRagEnhanceJava` 接口，解析 AI 返回的 JSON 并保存为模版记录

**关键发现**：AI 生成的内容是 canvas-editor 的 **元素数组 JSON 格式**（`main` 字段），而非普通文本。这意味着 AI 被 Prompt 约束为输出结构化的文档元素（含字体、颜色、排版信息），而不是纯文本。

**现有 `TestAiGenWordEnhance` 的核心逻辑**：

```java
@Component("jeecgDemoAiWordGen")
public class TestAiGenWordEnhance implements IAiRagEnhanceJava {
    @Override
    public Map<String, Object> process(Map<String, Object> inputParams) {
        // 1. 获取 AI 返回的内容
        Object resp = inputParams.get("resp");
        String respStr = String.valueOf(resp);

        // 2. 从 AI 返回中提取 JSON 数组（canvas-editor 元素格式）
        Matcher matcher = Pattern.compile("\\[.*]", Pattern.DOTALL).matcher(respStr);
        if (matcher.find()) {
            String mainStr = matcher.group();
            // 3. 清洗：替换中文双引号、NBSP 等
            mainStr = mainStr.replaceAll("[\"\"]", "\"");
            mainStr = mainStr.replaceAll("\\u00A0", " ");
            // 4. 校验 JSON 合法性
            JSON.parse(mainStr);

            // 5. 保存为模版记录
            EoaWordTemplate template = new EoaWordTemplate();
            template.setName("AI生成的简历_" + DateUtils.formatDate());
            template.setCode("AI_GEN_" + System.currentTimeMillis());
            template.setHeader("[]");
            template.setFooter("[]");
            template.setMain(mainStr);
            template.setWidth(794);
            template.setHeight(1123);
            template.setMargins("[100,120,100,120]");
            template.setPaperDirection("vertical");
            eoaWordTemplateService.save(template);
        }
        return Collections.singletonMap("result", "success");
    }
}
```

### 5.9.2 通用 AI 文档生成扩展方案

**完全可以复用现有机制**，只需扩展两处：

1. **新建通用 Java 增强节点**：替代 `TestAiGenWordEnhance`，支持多种文档类型
2. **为每种文档类型编写专用 Prompt**：让 AI 输出对应格式的 canvas-editor JSON

**扩展后的 AI 文档生成流程**：

```
用户选择模版 + 输入需求描述
       │
       ▼
前端调用 → POST /fe/document/aiGenerate
       │
       ▼
后端组装参数 → 调用 AI 流程编排 (airag/flow/run)
       │
       ├── flowInputs 中注入:
       │   - templateType: "meeting_minutes" (文档类型)
       │   - userInput: "讨论了Q3技术路线..." (用户输入)
       │   - templateVariables: {会议主题, 参会人员...} (模版变量)
       │   - styleGuide: "公司正式文档风格" (风格约束)
       │
       ▼
AI 流程执行 → LLM 节点根据 Prompt 生成 canvas-editor JSON
       │
       ▼
Java 增强节点 (AiDocumentGenEnhance)
       │
       ├── 解析 AI 返回的 JSON
       ├── 保存为 fe_document 记录（而非模版）
       ├── 关联来源模版 template_id
       └── 返回 documentId
       │
       ▼
前端跳转到文档编辑页 → 用户可继续修改
```

### 5.9.3 通用 Java 增强节点实现

```java
@Component("aiDocumentGenEnhance")
public class AiDocumentGenEnhance implements IAiRagEnhanceJava {

    @Autowired
    IFeDocumentService feDocumentService;

    @Autowired
    IEoaWordTemplateService eoaWordTemplateService;

    @Override
    public Map<String, Object> process(Map<String, Object> inputParams) {
        Object resp = inputParams.get("resp");
        String respStr = String.valueOf(resp);
        if (oConvertUtils.isEmpty(respStr)) {
            throw new JeecgBootException("AI生成内容失败，请稍后再试");
        }

        // 提取 JSON 数组（canvas-editor 元素格式）
        String mainStr = extractJsonArray(respStr);

        // 获取参数
        String templateType = (String) inputParams.getOrDefault("templateType", "general");
        String documentTitle = (String) inputParams.getOrDefault("documentTitle",
            "AI生成文档_" + DateUtils.formatDate());
        String templateId = (String) inputParams.get("templateId");
        String userId = (String) inputParams.get("userId");

        // 创建文档实例（而非模版）
        FeDocument document = new FeDocument();
        document.setTitle(documentTitle);
        document.setTemplateId(templateId);
        document.setDocType("word");
        document.setStatus("draft");
        document.setVisibility("private");
        document.setVersion(1);
        document.setHeader("[]");
        document.setFooter("[]");
        document.setMain(mainStr);
        document.setWidth(794);
        document.setHeight(1123);
        document.setMargins("[100,120,100,120]");
        document.setPaperDirection("vertical");
        document.setCreateBy(userId);
        feDocumentService.save(document);

        return Collections.singletonMap("documentId", document.getId());
    }

    private String extractJsonArray(String respStr) {
        Matcher matcher = Pattern.compile("\\[.*]", Pattern.DOTALL).matcher(respStr);
        if (matcher.find()) {
            String mainStr = matcher.group();
            mainStr = mainStr.replaceAll("[\"\"]", "\"");
            mainStr = mainStr.replaceAll("\\u00A0", " ");
            try {
                JSON.parse(mainStr);
            } catch (Exception e) {
                throw new JeecgBootException("AI生成的内容不是合法的JSON，请优化提示词");
            }
            return mainStr;
        }
        throw new JeecgBootException("AI生成的内容不是合法的JSON，请优化提示词");
    }
}
```

### 5.9.4 AI 文档生成 Prompt 模板

每种文档类型需要专用 Prompt，约束 AI 输出 canvas-editor 的 JSON 元素格式：

| 文档类型 | Prompt 核心约束 | 输出格式 |
|---------|----------------|---------|
| 个人简历 | 输出简历结构：标题+个人信息+教育+经历+技能 | canvas-editor JSON |
| 会议纪要 | 输出会议结构：标题+参会人+议题+决议+待办 | canvas-editor JSON |
| 技术方案 | 输出方案结构：标题+背景+方案+架构+排期 | canvas-editor JSON |
| 周报月报 | 输出周报结构：标题+本周工作+问题+下周计划 | canvas-editor JSON |
| 项目立项 | 输出立项结构：标题+背景+目标+方案+预算 | canvas-editor JSON |

**Prompt 示例（会议纪要）**：

```
你是一个专业的会议纪要撰写助手。根据用户提供的会议信息，生成一份格式规范的会议纪要。

你必须输出 canvas-editor 的 JSON 元素数组格式，严格遵循以下结构：

[
  {"value":"会议纪要","size":26,"bold":true,"rowFlex":"center"},
  {"value":"\n"},
  {"value":"会议主题：","bold":true},{"value":"{{会议主题}}"},
  {"value":"\n"},
  {"value":"会议时间：","bold":true},{"value":"{{会议时间}}"},
  ...
]

规则：
1. 标题用 size=26, bold=true, rowFlex=center
2. 小标题用 size=16, bold=true
3. 正文用 size=14
4. 换行用 {"value":"\n"}
5. 只输出 JSON 数组，不要输出任何其他内容
6. 不要使用 Markdown 格式

用户输入：{userInput}
```

### 5.9.5 AI 文档生成方式

提供三种生成方式，满足不同场景：

| 方式 | 交互流程 | 适用场景 |
|------|---------|---------|
| **从模版一键生成** | 选模版 → 填变量 → AI 生成 | 标准文档，如周报、会议纪要 |
| **对话式生成** | 与 AI 对话 → 逐步完善 → 生成文档 | 复杂文档，如技术方案 |
| **导入 Word 后 AI 润色** | 上传 Word → AI 解析 → 优化排版和措辞 | 老文档升级改造 |

**方式一：从模版一键生成**（最常用）

```
用户选择"会议纪要"模版 → 填写变量：
  ┌──────────────────────────────────────┐
  │  📝 AI 生成会议纪要                    │
  │                                      │
  │  会议主题: [Q3技术路线讨论         ]   │
  │  会议时间: [2025-05-11 14:00      ]   │
  │  参会人员: [张三、李四、王五       ]   │
  │  会议要点: [讨论了微服务改造方案... ]   │
  │                                      │
  │  ☑ AI 自动生成完整内容                 │
  │                                      │
  │  [取消]  [AI 生成]                     │
  └──────────────────────────────────────┘
```

**方式二：对话式生成**（类似 Chat2Code）

```
用户: "帮我写一份关于微服务改造的技术方案"
  AI: "好的，我将生成技术方案文档。请问重点是什么？"
用户: "重点是服务拆分策略和数据库迁移方案"
  AI: [生成完整文档] → 用户点击"保存为文档"
```

**方式三：导入 Word 后 AI 润色**

```
用户上传 老版文档.docx → 系统解析为 canvas-editor JSON
  → 用户点击"AI 润色" → 选择润色类型：排版优化/措辞优化/格式规范化
  → AI 修改后 → 用户确认 → 保存
```

### 5.9.6 后端接口

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| AI 生成文档 | POST | `/fe/document/aiGenerate` | 一键生成（指定模版类型+变量） |
| AI 对话生成 | POST | `/fe/document/aiChatGenerate` | 对话式生成（返回 SSE 流） |
| AI 润色文档 | POST | `/fe/document/aiPolish` | 对已有文档 AI 优化 |
| AI 续写文档 | POST | `/fe/document/aiContinue` | AI 续写文档内容 |

**AI 生成文档接口入参**：

```java
@Data
public class AiDocumentGenDTO {
    String templateType;            // 文档类型: resume/meeting_minutes/tech_proposal/weekly_report...
    String documentTitle;           // 文档标题
    String templateId;              // 关联模版ID(可选)
    Map<String, Object> variables;  // 模版变量值
    String styleGuide;              // 风格约束(可选): formal=正式, casual=轻松, technical=技术
    String modelId;                 // 指定AI模型(可选, 默认用系统默认模型)
}
```

---

## 6. 私有文档权限管理（分享驱动模型）

### 6.1 核心原则：默认私有，分享才能访问

**文档创建后默认只有创建者自己能看到**，其他人完全不可见。只有创建者主动"分享"给某人，该人才能看到并按授权权限操作。

```
创建文档 → 默认 visibility=private → 只有创建者可见
       │
       ├── 分享给张三(edit) → 张三可查看+编辑
       ├── 分享给李四(view) → 李四只能查看
       ├── 分享给研发部(edit) → 研发部所有人可查看+编辑
       ├── 生成分享链接 → 知道链接+密码的人可访问
       └── 设为 public → 全员可见（仍需单独授权才能编辑）
```

**可见性三档**：

| 可见性 | 说明 | 谁能看到 | 谁能编辑 |
|--------|------|---------|---------|
| `private`（默认） | 私有文档 | 仅创建者 | 仅创建者 |
| `shared` | 已分享文档 | 创建者 + 被分享人 | 创建者 + 被 share edit 权限的人 |
| `public` | 公开文档 | 全员可见 | 创建者 + 被 share edit 权限的人 |

**关键约束**：
- 文档创建时 `visibility` 强制为 `private`
- 当第一次执行"分享"操作时，`visibility` 自动变为 `shared`
- 只有创建者才能将文档设为 `public`
- 即使 `visibility=public`，编辑/导出/管理等权限仍需单独授权
- 创建者删除所有分享后，`visibility` 自动回退为 `private`

### 6.2 分享方式

| 分享方式 | target_type | 说明 | 适用场景 |
|---------|-------------|------|---------|
| 指定用户 | `user` | 分享给某个具体用户 | 点对点协作 |
| 指定角色 | `role` | 分享给某个角色下所有用户 | 按职能协作（如：所有前端开发） |
| 指定部门 | `dept` | 分享给某个部门所有用户 | 按部门协作 |
| 分享链接 | `link` | 生成带密码的链接，任何人可通过链接访问 | 外部协作、临时分享 |

### 6.3 权限矩阵

| 角色 | 查看 | 编辑 | 导出 | 删除 | 权限管理(分享) | 强制解锁 |
|------|:---:|:---:|:---:|:---:|:---:|:---:|
| 文档所有者 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 被 share manage | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 被 share edit | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| 被 share export | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 被 share view | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 通过分享链接(view) | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 通过分享链接(edit) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| public 可见但未授权 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 其他用户（未分享） | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

### 6.4 分享交互流程

**方式一：指定用户/角色/部门分享**

```
创建者点击"分享"按钮
       │
       ▼
弹出分享弹窗：
  ┌──────────────────────────────────────┐
  │  分享文档: 技术方案.docx              │
  │                                      │
  │  添加协作者:                          │
  │  ┌────────────────────────────────┐  │
  │  │ 🔍 搜索用户/角色/部门...       │  │
  │  └────────────────────────────────┘  │
  │                                      │
  │  已添加:                              │
  │  👤 张三  [编辑 ▼]  [×]              │
  │  👤 李四  [查看 ▼]  [×]              │
  │  🏢 研发部 [编辑 ▼]  [×]             │
  │                                      │
  │  ☑ 通知被分享人                       │
  │                                      │
  │  [取消]  [确认分享]                    │
  └──────────────────────────────────────┘
       │
       ▼
确认分享 → 写入 fe_document_permission → 文档 visibility 变为 shared
       │
       ▼（如果勾选了通知）
通知被分享人 → 站内消息 + (可选)邮件通知
       │
       ▼
被分享人登录 → "与我共享"列表中看到该文档
```

**方式二：分享链接**

```
创建者点击"生成分享链接"
       │
       ▼
弹出链接分享弹窗：
  ┌──────────────────────────────────────┐
  │  分享链接: 技术方案.docx              │
  │                                      │
  │  链接权限: [查看 ▼]                   │
  │                                      │
  │  🔒 访问密码: [自动生成] [自定义]     │
  │     密码: a3k9                       │
  │                                      │
  │  ⏰ 有效期: [7天 ▼]                   │
  │                                      │
  │  链接地址:                            │
  │  https://xxx/doc/s/abc123            │
  │  [复制链接]                           │
  │                                      │
  │  [取消]  [创建链接]                    │
  └──────────────────────────────────────┘
       │
       ▼
创建链接 → 写入 fe_document_permission (target_type=link)
       │
       ▼
访问者打开链接 → 输入密码 → 校验通过 → 按链接权限访问文档
```

### 6.5 权限校验逻辑

```java
@Service
public class DocumentPermissionServiceImpl implements IFeDocumentPermissionService {

    /**
     * 校验用户对文档的权限
     * 核心原则：默认不可访问，除非满足以下任一条件
     */
    public boolean hasPermission(String documentId, String userId, String requiredPermission) {
        FeDocument doc = documentService.getById(documentId);
        if (doc == null) return false;

        // 1. 文档所有者拥有全部权限
        if (userId.equals(doc.getCreateBy())) return true;

        // 2. 检查是否被直接分享
        long count = permissionMapper.selectCount(
            Wrappers.lambdaQuery(FeDocumentPermission.class)
                .eq(FeDocumentPermission::getDocumentId, documentId)
                .eq(FeDocumentPermission::getTargetType, "user")
                .eq(FeDocumentPermission::getTargetId, userId)
                .eq(FeDocumentPermission::getPermission, requiredPermission)
                .and(w -> w.isNull(FeDocumentPermission::getExpireTime)
                    .or().gt(FeDocumentPermission::getExpireTime, new Date()))
        );
        if (count > 0) return true;

        // 3. 检查是否通过角色分享
        List<String> userRoleIds = getUserRoleIds(userId);
        if (!userRoleIds.isEmpty()) {
            count = permissionMapper.selectCount(
                Wrappers.lambdaQuery(FeDocumentPermission.class)
                    .eq(FeDocumentPermission::getDocumentId, documentId)
                    .eq(FeDocumentPermission::getTargetType, "role")
                    .in(FeDocumentPermission::getTargetId, userRoleIds)
                    .eq(FeDocumentPermission::getPermission, requiredPermission)
            );
            if (count > 0) return true;
        }

        // 4. 检查是否通过部门分享
        List<String> userDeptIds = getUserDeptIds(userId);
        if (!userDeptIds.isEmpty()) {
            count = permissionMapper.selectCount(
                Wrappers.lambdaQuery(FeDocumentPermission.class)
                    .eq(FeDocumentPermission::getDocumentId, documentId)
                    .eq(FeDocumentPermission::getTargetType, "dept")
                    .in(FeDocumentPermission::getTargetId, userDeptIds)
                    .eq(FeDocumentPermission::getPermission, requiredPermission)
            );
            if (count > 0) return true;
        }

        // 5. public 文档允许查看，但其他权限仍需授权
        if ("public".equals(doc.getVisibility()) && "view".equals(requiredPermission)) {
            return true;
        }

        // 6. 以上都不满足 → 无权限
        return false;
    }

    /**
     * 通过分享链接校验权限
     */
    public PermissionCheckResult checkLinkPermission(String shareLink, String password) {
        FeDocumentPermission perm = permissionMapper.selectOne(
            Wrappers.lambdaQuery(FeDocumentPermission.class)
                .eq(FeDocumentPermission::getShareLink, shareLink)
        );
        if (perm == null) {
            return PermissionCheckResult.fail("链接无效");
        }
        if (perm.getShareLinkExpire() != null && perm.getShareLinkExpire().before(new Date())) {
            return PermissionCheckResult.fail("链接已过期");
        }
        if (perm.getShareLinkPassword() != null && !perm.getShareLinkPassword().equals(password)) {
            return PermissionCheckResult.fail("密码错误");
        }
        return PermissionCheckResult.ok(perm.getPermission());
    }

    /**
     * 数据权限过滤：只返回当前用户有权限的文档
     */
    public String getPermissionSql(String userId, List<String> roleIds, List<String> deptIds) {
        return "d.id IN (" +
            "SELECT d2.id FROM fe_document d2 " +
            "LEFT JOIN fe_document_permission p ON d2.id = p.document_id " +
            "WHERE d2.create_by = '" + userId + "' " +
            "OR d2.visibility = 'public' " +
            "OR (p.target_type = 'user' AND p.target_id = '" + userId + "' " +
            "    AND (p.expire_time IS NULL OR p.expire_time > NOW())) " +
            "OR (p.target_type = 'role' AND p.target_id IN (" + join(roleIds) + ")) " +
            "OR (p.target_type = 'dept' AND p.target_id IN (" + join(deptIds) + "))" +
            ")";
    }
}
```

### 6.6 分享通知

当创建者分享文档给某人时，系统自动发送通知：

| 通知方式 | 触发条件 | 通知内容 |
|---------|---------|---------|
| 站内消息 | 分享给用户/角色/部门 且 notify_target=1 | "张三 与你共享了文档《技术方案》" |
| 邮件通知 | 用户配置了邮件接收 | 同上 + 文档链接 |
| WebSocket 推送 | 被分享人在线 | 实时弹窗提醒 |

### 6.7 分享管理

创建者可随时管理分享状态：

```
文档详情页 → "分享管理"标签页
  ┌──────────────────────────────────────────────────────┐
  │  📄 技术方案.docx                                     │
  │  可见性: [private ▼]                                  │
  │                                                      │
  │  分享列表:                              [添加分享]     │
  │  ┌────────────────────────────────────────────────┐  │
  │  │ 👤 张三    编辑   2025-05-10分享   [撤销]       │  │
  │  │ 👤 李四    查看   2025-05-08分享   [撤销]       │  │
  │  │ 🏢 研发部  编辑   2025-05-06分享   [撤销]       │  │
  │  │ 🔗 链接    查看   7天后过期        [复制] [撤销] │  │
  │  └────────────────────────────────────────────────┘  │
  └──────────────────────────────────────────────────────┘
```

---

## 7. 文档模版功能设计

### 7.1 预置系统模版

| 模版 | 分类 | 变量 | 说明 |
|------|------|------|------|
| 个人简历 | 人事 | `{{姓名}}`, `{{电话}}`, `{{邮箱}}`, `{{教育经历}}`... | 复用现有 AI 简历功能 |
| 会议纪要 | 会议 | `{{会议主题}}`, `{{参会人员}}`, `{{会议时间}}`... | 新增 |
| 周报/月报 | 通用 | `{{姓名}}`, `{{本周工作}}`, `{{下周计划}}`... | 新增 |
| 技术方案 | 技术 | `{{项目名称}}`, `{{方案概述}}`, `{{架构设计}}`... | 新增 |
| 项目立项书 | 通用 | `{{项目名称}}`, `{{负责人}}`, `{{目标}}`... | 新增 |
| 需求文档 | 技术 | `{{需求名称}}`, `{{优先级}}`, `{{详细描述}}`... | 新增 |
| 出差申请 | 人事 | `{{姓名}}`, `{{目的地}}`, `{{日期}}`... | 新增 |
| 采购申请 | 财务 | `{{申请人}}`, `{{物品}}`, `{{金额}}`... | 新增 |

### 7.2 "从模版创建文档"流程

```
用户点击"从模版创建"
        │
        ▼
选择模版 → 预览模版 + 查看需要填写的变量
        │
        ▼
方式1: 手动填写变量 → 生成文档
方式2: AI 填写 → 调用 AI 流程自动填充变量 → 生成文档
        │
        ▼
文档创建成功 → 跳转到文档编辑页 → 可继续修改
        │
        ▼
编辑完成 → 导出 Word / 保存 / 分享
```

---

## 8. 后端接口设计

### 8.1 文档管理接口

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 从模版创建文档 | POST | `/fe/document/create` | 支持空模版创建空白文档 |
| 文档列表 | GET | `/fe/document/list` | 自动注入权限过滤 |
| 编辑文档 | PUT | `/fe/document/edit` | 需校验编辑权限 + 编辑锁 |
| 删除文档 | DELETE | `/fe/document/delete` | 需校验管理权限 |
| 查看文档详情 | GET | `/fe/document/queryById` | 需校验查看权限 |
| 导出 Word | POST | `/fe/document/exportWord` | 需校验导出权限 |
| 查看版本历史 | GET | `/fe/document/versions` | 查看文档版本列表 |
| 回滚版本 | POST | `/fe/document/rollback` | 回滚到指定版本 |
| 获取编辑锁 | POST | `/fe/document/acquireLock` | 打开编辑器时调用 |
| 释放编辑锁 | POST | `/fe/document/releaseLock` | 关闭编辑器时调用 |
| 编辑心跳 | POST | `/fe/document/heartbeat` | 编辑期间每15秒调用 |
| 强制释放锁 | POST | `/fe/document/forceReleaseLock` | 管理员/所有者可操作 |

### 8.2 权限与分享管理接口

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 分享文档 | POST | `/fe/document/permission/share` | 指定用户/角色/部门 + 授权权限 |
| 生成分享链接 | POST | `/fe/document/permission/shareLink` | 生成带密码的分享链接 |
| 校验分享链接 | POST | `/fe/document/permission/checkLink` | 访问者校验链接+密码 |
| 撤销分享 | DELETE | `/fe/document/permission/revoke` | 撤销某条分享授权 |
| 批量分享 | POST | `/fe/document/permission/batchShare` | 批量授权（如：分享给整个部门） |
| 查看文档分享列表 | GET | `/fe/document/permission/listByDoc` | 查看文档的所有分享记录 |
| 查看与我共享 | GET | `/fe/document/permission/sharedWithMe` | 当前用户被分享的文档列表 |
| 修改可见性 | PUT | `/fe/document/visibility` | 修改文档可见性(private/shared/public) |

### 8.3 批注管理接口

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 添加批注 | POST | `/fe/document/comment/add` | 选中文字 → 写批注 |
| 回复批注 | POST | `/fe/document/comment/reply` | 回复已有批注 |
| 标记解决 | POST | `/fe/document/comment/resolve` | 标记批注为已解决 |
| 重新打开 | POST | `/fe/document/comment/reopen` | 重新打开已解决的批注 |
| 删除批注 | DELETE | `/fe/document/comment/delete` | 删除批注 |
| 文档批注列表 | GET | `/fe/document/comment/listByDoc` | 按位置分组返回 |
| 批注线索回复 | GET | `/fe/document/comment/listByGroup` | 某批注的全部回复 |

### 8.4 文件夹管理接口

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 文件夹树 | GET | `/fe/document/folder/tree` | 获取文件夹树形结构 |
| 创建文件夹 | POST | `/fe/document/folder/add` | 新增文件夹 |
| 编辑文件夹 | PUT | `/fe/document/folder/edit` | 修改文件夹 |
| 删除文件夹 | DELETE | `/fe/document/folder/delete` | 删除空文件夹 |

### 8.5 模版管理接口（扩展现有）

| 接口 | Method | Path | 说明 |
|------|--------|------|------|
| 模版市场列表 | GET | `/fe/document/template/market` | 按分类浏览模版 |
| 模版分类列表 | GET | `/fe/document/template/categories` | 获取模版分类 |
| 模版使用计数 | POST | `/fe/document/template/incrementUse` | 使用次数+1 |

---

## 9. 前端页面设计

### 9.1 页面清单

| 页面 | 路由 | 说明 |
|------|------|------|
| 文档中心 | `/fe/doc/list` | 左侧文件夹树 + 右侧文档列表（卡片/列表双视图） |
| 我的文档 | `/fe/doc/mine` | 当前用户创建的私有文档 |
| 与我共享 | `/fe/doc/shared` | 其他人分享给我的文档 |
| 文档编辑 | `/fe/doc/edit/:id` | 在线编辑器（复用 DocDesign 组件 + 协同状态栏 + 批注面板） |
| 文档详情 | `/fe/doc/detail/:id` | 只读查看 + 版本历史 + 分享管理 |
| 分享链接访问 | `/fe/doc/s/:token` | 通过分享链接访问文档（需输入密码） |
| 模版市场 | `/fe/doc/templates` | 模版分类浏览 + 搜索 + 一键创建文档 |
| 模版管理 | `/fe/doc/template-admin` | 管理模版（仅管理员） |

### 9.2 文档编辑页面布局

```
┌─────────────────────────────────────────────────────────────────┐
│ 工具栏: 保存 | 撤销/重做 | 字体 | 字号 | 对齐 | ... | 导出Word │
├──────────┬────────────────────────────────────┬────────────────┤
│          │                                    │                │
│ 协同状态 │                                    │   批注面板     │
│ ┌──────┐ │                                    │ ┌────────────┐ │
│ │在线: │ │      文档编辑区                    │ │ ① 批注1    │ │
│ │🟢张三│ │      (canvas-editor)              │ │   回复/解决 │ │
│ │🟡李四│ │                                    │ ├────────────┤ │
│ │(编辑)│ │                                    │ │ ② 批注2    │ │
│ └──────┘ │                                    │ │   回复/解决 │ │
│          │                                    │ └────────────┘ │
│ 锁状态:  │                                    │                │
│ 📝张三   │                                    │  [添加批注]    │
│ 正在编辑  │                                    │                │
└──────────┴────────────────────────────────────┴────────────────┘
```

### 9.3 协同状态栏组件

```vue
<template>
  <div class="collab-status-bar" v-if="collabInfo">
    <div class="lock-info">
      <span v-if="collabInfo.isEditing" class="editing-badge">
        <a-badge status="processing" /> 你正在编辑
      </span>
      <span v-else-if="collabInfo.lockHolder" class="locked-badge">
        <a-badge status="warning" /> {{ collabInfo.lockHolderName }} 正在编辑
        <a-button size="small" v-if="collabInfo.canForceUnlock" @click="forceUnlock">
          强制接管
        </a-button>
      </span>
      <span v-else class="free-badge">
        <a-badge status="success" /> 可编辑
      </span>
    </div>
    <div class="online-users">
      <a-avatar-group :max-count="5" size="small">
        <a-tooltip v-for="user in collabInfo.onlineUsers" :key="user.id" :title="user.name">
          <a-avatar :src="user.avatar" />
        </a-tooltip>
      </a-avatar-group>
    </div>
  </div>
</template>
```

---

## 10. 后端模块结构

```
org.jeecg.modules.airag.wordtpl/
├── controller/
│   ├── EoaWordTemplateController.java         (已有：模版管理，扩展分类/标签)
│   ├── FeDocumentController.java              (新增：文档CRUD + 编辑锁)
│   ├── FeDocumentPermissionController.java     (新增：权限管理)
│   ├── FeDocumentFolderController.java         (新增：文件夹管理)
│   ├── FeDocumentCommentController.java        (新增：批注管理)
│   └── FeDocumentCollabController.java         (新增：协同编辑WebSocket)
├── entity/
│   ├── EoaWordTemplate.java                   (已有，扩展字段)
│   ├── FeDocument.java                        (新增)
│   ├── FeDocumentPermission.java              (新增)
│   ├── FeDocumentFolder.java                  (新增)
│   ├── FeDocumentLock.java                    (新增)
│   ├── FeDocumentVersion.java                 (新增)
│   ├── FeDocumentComment.java                 (新增)
│   └── FeTemplateCategory.java                (新增)
├── dto/
│   ├── WordTplGenDTO.java                     (已有)
│   ├── DocumentCreateDTO.java                 (新增：从模版创建文档入参)
│   ├── DocumentPermissionDTO.java             (新增：权限授权入参)
│   ├── CommentGroupVO.java                    (新增：批注分组返回结构)
│   └── CollabHeartbeatDTO.java                (新增：协同心跳)
├── service/
│   ├── IEoaWordTemplateService.java           (已有，扩展方法)
│   ├── IFeDocumentService.java                (新增)
│   ├── IFeDocumentPermissionService.java      (新增)
│   ├── IFeDocumentFolderService.java          (新增)
│   ├── IFeDocumentLockService.java            (新增)
│   └── IFeDocumentCommentService.java         (新增)
├── service/impl/
│   └── ... (对应实现)
├── websocket/
│   └── DocumentCollabWebSocket.java           (新增：协同编辑WS端点)
└── utils/
    └── WordTplUtils.java                      (已有，扩展批注导出)
```

---

## 11. 技术风险与应对

| 风险 | 说明 | 应对方案 |
|------|------|---------|
| canvas-editor 不支持光标同步 | 内部状态管理封闭 | Phase 7 可通过 `instance.command.executeInsert` 等API做增量同步 |
| 大文档性能 | main 字段 JSON 含图片 base64 可能很大 | 图片走 OSS/MinIO 存储，main 中只存 URL；分页加载编辑 |
| Word 导出格式保真度 | canvas-editor JSON → POI Word 有格式损失 | 复杂格式走"先上传 Word 模版 → 在线编辑 → 导出"路径 |
| WebSocket 集群部署 | 多节点时 WS 消息需跨节点转发 | 引入 Redis Pub/Sub 做 WS 消息广播 |
| 编辑锁高可用 | 服务重启导致锁丢失 | 锁存储 MySQL + Redis 双写，Redis 做快速判断，MySQL 做持久化兜底 |
| 批注定位偏移 | 文档内容修改后 startIndex/endIndex 可能失效 | 保存 selectedText 冗余；编辑保存时触发批注位置校准 |
| 与现有 AI 简历功能兼容 | 不能破坏现有 `aigc_word_template` 功能 | 新增 `fe_document` 独立表，不修改现有表结构（只增加字段） |
| 分享链接泄露 | 分享链接被恶意传播导致文档泄露 | 链接必须设置密码；链接可设过期时间；创建者可随时撤销；链接访问记录审计日志 |
| 权限越级 | 被 share edit 的用户是否可以再分享给他人 | 默认不允许二次分享（仅 manage 权限和所有者可分享）；可选配置"允许协作者邀请" |

---

## 12. 实施路线图

| 阶段 | 内容 | 优先级 |
|------|------|--------|
| **Phase 1** | 文档实例表 + 基础 CRUD + 从模版创建文档 + Word 导出 | P0 |
| **Phase 2** | 文件夹管理 + 文档列表页 + 私有文档权限管理 | P0 |
| **Phase 3** | 编辑锁 + WebSocket 协同通知 + 在线用户显示 | P1 |
| **Phase 4** | 批注功能（选中→评论→高亮→回复→解决） | P1 |
| **Phase 5** | 批注导出到 Word + 版本历史 + 文档对比 | P1 |
| **Phase 6** | 模版市场 + 系统预置模版 + AI 自动填充 | P2 |
| **Phase 7** | 光标位置同步 + 选区同步（轻量协同） | P3 |
