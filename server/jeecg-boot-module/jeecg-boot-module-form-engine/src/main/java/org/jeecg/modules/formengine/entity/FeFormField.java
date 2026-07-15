package org.jeecg.modules.formengine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

//update-begin---author:formengine ---date:2026-07-08  for：【表单引擎】表单字段实体---
@Data
@TableName("fe_form_field")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "表单字段")
public class FeFormField implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "模版ID")
    private String templateId;

    @Schema(description = "字段Key")
    private String fieldKey;

    @Schema(description = "字段标签")
    private String fieldLabel;

    @Schema(description = "字段类型")
    private String fieldType;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "权限(JSON)")
    private String permissions;

    @Schema(description = "排序号")
    private Integer sortNo;
}
//update-end---author:formengine ---date:2026-07-08  for：【表单引擎】表单字段实体---
