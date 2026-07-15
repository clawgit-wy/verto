-- =====================================================================
-- 表单引擎 - 流程级Schema字段定义表 (MySQL 8.0)
-- 说明：存储每个流程的表单字段Schema定义（流程级，所有节点共享同一套字段定义）
--       对应前端 NodeFormField 接口：fieldKey/fieldLabel/fieldType/defaultValue/
--       required/pattern/min/max/validationMessage/description
--       六种字段类型：string/number/boolean/date/stringArray/numberArray
-- =====================================================================

CREATE TABLE IF NOT EXISTS fe_process_schema_field (
    id                 VARCHAR(64)  NOT NULL COMMENT '主键ID',
    process_id         VARCHAR(64)  NOT NULL COMMENT '流程ID（关联 fe_process.id）',
    field_key          VARCHAR(100) NOT NULL COMMENT '字段标识（唯一key，如 applyReason）',
    field_label        VARCHAR(200)          COMMENT '字段显示名称（如 申请原因）',
    field_type         VARCHAR(50)  NOT NULL COMMENT '字段类型: string/number/boolean/date/stringArray/numberArray',
    default_value      VARCHAR(500)          COMMENT '默认值',
    required           TINYINT(1)   DEFAULT 0 COMMENT '是否必填: 0=否, 1=是',
    pattern            VARCHAR(500)          COMMENT '正则校验表达式（可选）',
    min_value          INT                   COMMENT '最小值（数字）或最小长度（字符）',
    max_value          INT                   COMMENT '最大值（数字）或最大长度（字符）',
    validation_message VARCHAR(500)          COMMENT '验证失败提示信息',
    description        VARCHAR(500)          COMMENT '字段说明',
    sort_no            INT          DEFAULT 0 COMMENT '排序号',
    create_by          VARCHAR(50)           COMMENT '创建人',
    create_time        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by          VARCHAR(50)           COMMENT '更新人',
    update_time        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_process_id (process_id),
    UNIQUE KEY uk_process_field (process_id, field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='表单引擎-流程级Schema字段定义';

-- =====================================================================
-- 同步扩展 fe_process 表：增加 form_schema_fields 字段（缓存JSON，便于设计器快速加载）
-- 注意：fe_process_schema_field 表为权威数据源，form_schema_fields 为冗余缓存
-- =====================================================================
-- MySQL 8.0 不支持 ADD COLUMN IF NOT EXISTS，使用存储过程兼容已存在的情况
DROP PROCEDURE IF EXISTS fe_add_column_if_not_exists;
DELIMITER $$
CREATE PROCEDURE fe_add_column_if_not_exists()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'fe_process'
          AND COLUMN_NAME = 'form_schema_fields'
    ) THEN
        ALTER TABLE fe_process ADD COLUMN form_schema_fields TEXT COMMENT '流程级表单Schema字段定义（JSON缓存，权威数据在fe_process_schema_field表）';
    END IF;
END$$
DELIMITER ;
CALL fe_add_column_if_not_exists();
DROP PROCEDURE IF EXISTS fe_add_column_if_not_exists;
