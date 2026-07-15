-- =====================================================================
-- 表单引擎与流程仿真测试工作台 - MySQL 8.0 初始化脚本
-- 对应设计：form-engine/design-proposal.md
-- 适用数据库：MySQL 8.0+（本机 Docker 实例 infra-pool-mysql80 / 库 jeecg_boot）
-- 说明：从 PostgreSQL 版（V04__formengine_postgres.sql）回迁至 MySQL，
--       主键采用字符串 ASSIGN_ID（由 MyBatis-Plus 雪花算法生成），此处直接写入固定 ID 便于演示。
-- =====================================================================

-- 1. 流程列表（行级主入口）
CREATE TABLE IF NOT EXISTS fe_process (
    id            VARCHAR(64)  NOT NULL,
    process_name  VARCHAR(200) NOT NULL,
    process_code  VARCHAR(100),
    template_id   VARCHAR(64),
    template_name VARCHAR(200),
    version       VARCHAR(50),
    status        VARCHAR(20)  DEFAULT 'draft' COMMENT 'draft=草稿 imported=已导入 simulated=已仿真 exported=已导出',
    form_schema   TEXT         COMMENT 'form-create 设计器产出的 JSON Schema',
    process_def   TEXT         COMMENT '导入的流程定义 JSON（节点+表单绑定）',
    create_by     VARCHAR(50),
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_by     VARCHAR(50),
    update_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag      CHAR(1)      DEFAULT '0',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-流程列表';

-- 2. 表单模板
CREATE TABLE IF NOT EXISTS fe_form_template (
    id            VARCHAR(64)  NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    version       VARCHAR(50),
    description   VARCHAR(500),
    create_time   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-表单模板';

-- 3. 字段元数据（含权限矩阵）
CREATE TABLE IF NOT EXISTS fe_form_field (
    id            VARCHAR(64)  NOT NULL,
    template_id   VARCHAR(64)  NOT NULL,
    field_key     VARCHAR(100) NOT NULL,
    field_label   VARCHAR(200),
    field_type    VARCHAR(50)  COMMENT 'input/number/select/button...',
    default_value VARCHAR(500),
    permissions   TEXT         COMMENT 'JSON: {"applicant":"write","manager":"readonly","finance":"readonly"}',
    sort_no       INT          DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-字段元数据与权限矩阵';

-- 4. 业务方法（可绑定到按钮组件）
CREATE TABLE IF NOT EXISTS fe_form_method (
    id            VARCHAR(64)  NOT NULL,
    method_key    VARCHAR(200) NOT NULL,
    method_name   VARCHAR(200),
    params        TEXT         COMMENT 'JSON 数组，入参字段 key 列表',
    description   VARCHAR(500),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-可绑定业务方法';

-- =====================================================================
-- 种子数据
-- =====================================================================

-- 表单模板
INSERT IGNORE INTO fe_form_template (id, template_name, version, description) VALUES
('tmpl_001', '客车采购申请表单', 'v1.0', '客车采购申请流程配套表单'),
('tmpl_002', '售后维修派单表',   'v2.1', '售后维修派单流程配套表单'),
('tmpl_003', '请假申请表',       'v1.2', '员工请假审批流程配套表单'),
('tmpl_004', '费用报销单',       'v1.0', '费用报销审批流程配套表单');

-- 字段元数据（tmpl_001 客车采购申请表单）
INSERT IGNORE INTO fe_form_field (id, template_id, field_key, field_label, field_type, default_value, permissions, sort_no) VALUES
('ff_001', 'tmpl_001', 'bus_type',        '客车型号',       'input',  '',    '{"applicant":"write","manager":"readonly","finance":"readonly"}', 1),
('ff_002', 'tmpl_001', 'order_count',     '采购数量',       'number', '0',   '{"applicant":"write","manager":"readonly","finance":"readonly"}', 2),
('ff_003', 'tmpl_001', 'total_price',     '总金额',         'number', '0',   '{"applicant":"readonly","manager":"readonly","finance":"readonly"}', 3),
('ff_004', 'tmpl_001', 'manager_opinion', '经理审批意见',   'textarea','',    '{"applicant":"hidden","manager":"write","finance":"readonly"}',     4),
('ff_005', 'tmpl_001', 'finance_opinion', '财务复核意见',   'textarea','',    '{"applicant":"hidden","manager":"hidden","finance":"write"}',       5);

-- 业务方法
INSERT IGNORE INTO fe_form_method (id, method_key, method_name, params, description) VALUES
('fm_001', 'stockService.checkInventory',   '检查库存',     '["bus_type","order_count"]', '根据客车型号与数量校验库存是否充足'),
('fm_002', 'busService.calculateDiscount',  '计算折扣',     '["bus_type","order_count"]', '根据采购数量计算折扣并回写单价/总金额'),
('fm_003', 'financeService.calculateTotal', '计算总金额',   '["order_count"]',            '根据数量×单价计算总金额');

-- 流程列表（与原型 process-list.html 一致）
INSERT IGNORE INTO fe_process (id, process_name, process_code, template_id, template_name, version, status, form_schema, process_def, update_time) VALUES
('proc_001', '客车采购申请流程', 'PROC_BUS_001',   'tmpl_001', '客车采购申请表单', 'v1.0', 'imported',
 '{"templateId":"tmpl_001","templateName":"客车采购申请表单","version":"v1.0","layout":[{"id":"comp_1","type":"input","field":"bus_type","title":"客车型号","props":{"placeholder":"请输入","required":true}},{"id":"comp_2","type":"number","field":"order_count","title":"采购数量","props":{"required":true}},{"id":"comp_3","type":"number","field":"total_price","title":"总金额","props":{"disabled":true}},{"id":"comp_4","type":"textarea","field":"manager_opinion","title":"经理审批意见"}]}',
 '{"processDefId":"pd_bus_purchase","processName":"客车采购申请流程","nodes":[{"nodeId":"n1","name":"申请","role":"applicant","formTemplateId":"tmpl_001"},{"nodeId":"n2","name":"经理审批","role":"manager","formTemplateId":"tmpl_001"},{"nodeId":"n3","name":"财务复核","role":"finance","formTemplateId":"tmpl_001"}],"currentNode":"n1"}',
 '2026-07-06 09:30:00'),
('proc_002', '售后维修派单流程', 'PROC_REPAIR_002', 'tmpl_002', '售后维修派单表',   'v2.1', 'draft',
 NULL, NULL, '2026-07-05 16:12:00'),
('proc_003', '员工请假审批流程', 'PROC_LEAVE_003',  'tmpl_003', '请假申请表',       'v1.2', 'exported',
 NULL, NULL, '2026-07-04 11:45:00'),
('proc_004', '费用报销审批流程', 'PROC_EXP_004',    'tmpl_004', '费用报销单',       'v1.0', 'simulated',
 NULL, NULL, '2026-07-06 08:20:00');


-- =====================================================================
-- 5. airag_flow —— LiteFlow SQL 规则源表（AI 流编排）
-- 说明：jeecg-aiflow 模块的 application-liteflow.yml 通过 chainCustomSql 读取此表。
--       需建空表，否则 FlowExecutor 初始化失败
--       （查询条件 status='enable' AND chain IS NOT NULL，空表即不加载任何 AI 流，符合本次范围）。
-- =====================================================================
CREATE TABLE IF NOT EXISTS airag_flow (
    id               VARCHAR(64)  NOT NULL,
    application_name VARCHAR(64),
    chain            TEXT,
    status           VARCHAR(32) DEFAULT 'disable',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI流编排规则表（LiteFlow SQL规则源；空表，未启用AI流功能）';
