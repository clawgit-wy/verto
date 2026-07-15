package org.jeecg.modules.formengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】流程定义实体---
@Data
@TableName(value = "fe_process", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程定义")
public class FeProcess implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "流程名称")
    private String processName;

    @Schema(description = "流程编码")
    private String processCode;

    @Schema(description = "模版ID")
    private String templateId;

    @Schema(description = "模版名称")
    private String templateName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "状态: draft=草稿, imported=已导入")
    private String status;

    @Schema(description = "表单Schema(JSON)")
    private String formSchema;

    @Schema(description = "流程定义(JSON)")
    private String processDef;

    // update-begin---author:formengine ---date:2026-07-15  for：【表单引擎】扩展FeProcess，增加流程级表单Schema字段定义(JSON缓存)---
    @Schema(description = "流程级表单Schema字段定义（JSON缓存，权威数据在fe_process_schema_field表）")
    private String formSchemaFields;
    // update-end---author:formengine ---date:2026-07-15  for：【表单引擎】扩展FeProcess，增加流程级表单Schema字段定义(JSON缓存)---

    @Schema(description = "创建人")
    private String createBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private Date updateTime;

    @TableLogic
    @Schema(description = "删除标记")
    private String delFlag;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】流程定义实体---
