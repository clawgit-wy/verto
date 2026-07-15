package org.jeecg.modules.formengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】表单方法实体---
@Data
@TableName("fe_form_method")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "表单方法")
public class FeFormMethod implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "方法Key")
    private String methodKey;

    @Schema(description = "方法名称")
    private String methodName;

    @Schema(description = "参数(JSON)")
    private String params;

    @Schema(description = "描述")
    private String description;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】表单方法实体---
