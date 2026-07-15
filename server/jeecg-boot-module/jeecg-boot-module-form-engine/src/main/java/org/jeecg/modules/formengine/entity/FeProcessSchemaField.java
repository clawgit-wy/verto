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

/**
 * 流程级表单Schema字段定义实体
 * 对应前端 NodeFormField 接口，存储每个流程的表单字段Schema定义
 * 六种字段类型：string/number/boolean/date/stringArray/numberArray
 */
@Data
@TableName(value = "fe_process_schema_field")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "流程级表单Schema字段定义")
public class FeProcessSchemaField implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "流程ID（关联 fe_process.id）")
    private String processId;

    @Schema(description = "字段标识（唯一key，如 applyReason）")
    private String fieldKey;

    @Schema(description = "字段显示名称（如 申请原因）")
    private String fieldLabel;

    @Schema(description = "字段类型: string/number/boolean/date/stringArray/numberArray")
    private String fieldType;

    @Schema(description = "默认值")
    private String defaultValue;

    @Schema(description = "是否必填: 0=否, 1=是")
    private Integer required;

    @Schema(description = "正则校验表达式（可选）")
    private String pattern;

    @Schema(description = "最小值（数字）或最小长度（字符）")
    private Integer minValue;

    @Schema(description = "最大值（数字）或最大长度（字符）")
    private Integer maxValue;

    @Schema(description = "验证失败提示信息")
    private String validationMessage;

    @Schema(description = "字段说明")
    private String description;

    @Schema(description = "排序号")
    private Integer sortNo;

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
}
