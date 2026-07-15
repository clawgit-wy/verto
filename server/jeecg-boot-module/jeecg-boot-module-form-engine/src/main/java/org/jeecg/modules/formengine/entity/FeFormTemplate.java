package org.jeecg.modules.formengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】表单模版实体---
@Data
@TableName("fe_form_template")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "表单模版")
public class FeFormTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模版名称")
    private String templateName;

    @Schema(description = "版本")
    private String version;

    @Schema(description = "描述")
    private String description;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private Date createTime;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】表单模版实体---
