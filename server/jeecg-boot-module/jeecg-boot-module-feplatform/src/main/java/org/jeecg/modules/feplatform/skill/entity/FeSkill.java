package org.jeecg.modules.feplatform.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "fe_skill", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "AI Skill资产表")
public class FeSkill implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "Skill名称", width = 15)
    @Schema(description = "Skill名称")
    private String name;

    @Excel(name = "Skill编码", width = 15)
    @Schema(description = "Skill编码(唯一标识)")
    private String code;

    @Excel(name = "分类", width = 15)
    @Schema(description = "分类: official=官方, business=业务, app=应用级")
    private String category;

    @Schema(description = "图标")
    private String icon;

    @Excel(name = "Skill描述", width = 30)
    @Schema(description = "Skill描述")
    private String description;

    @Schema(description = "Prompt模板内容")
    private String promptTemplate;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "输入参数Schema(JSON)")
    private Map<String, Object> inputSchema;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "输出参数Schema(JSON)")
    private Map<String, Object> outputSchema;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "使用示例(JSON数组)")
    private List<Map<String, Object>> examples;

    @Schema(description = "关联应用ID(应用级Skill)")
    private String appId;

    @Excel(name = "版本号", width = 15)
    @Schema(description = "版本号")
    private String version;

    @Excel(name = "状态", width = 15)
    @Schema(description = "状态: enable/disable")
    private String status;

    @Schema(description = "排序号")
    private BigDecimal sortNo;

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
    @Schema(description = "删除标志")
    private Integer delFlag;

    @Schema(description = "所属部门")
    private String sysOrgCode;
}
