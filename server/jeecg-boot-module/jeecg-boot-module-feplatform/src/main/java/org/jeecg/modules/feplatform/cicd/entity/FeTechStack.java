package org.jeecg.modules.feplatform.cicd.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】技术栈实体---
@Data
@TableName("fe_tech_stack")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "技术栈")
public class FeTechStack implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "技术栈名称", width = 20)
    @Schema(description = "技术栈名称")
    private String name;

    @Excel(name = "技术栈编码", width = 20)
    @Schema(description = "技术栈编码: Vue2/Vue3/React/Angular/jQuery/MicroFrontend")
    private String code;

    @Excel(name = "Node版本范围", width = 20)
    @Schema(description = "Node版本范围, 如: v16-v18")
    private String nodeVersionRange;

    @Schema(description = "Lint配置(JSON)")
    private String lintConfig;

    @Schema(description = "Jenkinsfile模板")
    private String jenkinsfileTpl;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_tech_stack_status")
    @Schema(description = "状态: enable=启用, disable=停用")
    private String status;

    @Schema(description = "描述")
    private String description;

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
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】技术栈实体---
