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

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】Jenkins实例实体---
@Data
@TableName("fe_jenkins_instance")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Jenkins实例")
public class FeJenkinsInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "实例名称", width = 20)
    @Schema(description = "实例名称")
    private String name;

    @Excel(name = "Jenkins地址", width = 40)
    @Schema(description = "Jenkins访问地址")
    private String url;

    @Schema(description = "访问Token(加密存储)")
    private String token;

    @Excel(name = "域", width = 30)
    @Schema(description = "域")
    private String domain;

    @Excel(name = "环境类型", width = 15)
    @Dict(dicCode = "fe_jenkins_env_type")
    @Schema(description = "环境类型: test=测试环境, prod=生产环境")
    private String envType;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_jenkins_status")
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
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】Jenkins实例实体---
