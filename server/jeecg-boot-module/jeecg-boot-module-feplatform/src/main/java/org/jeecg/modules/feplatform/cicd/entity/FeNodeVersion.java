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

//update-begin---author:feplatform ---date:2026-05-22  for:【CICD治理】Node版本实体---
@Data
@TableName("fe_node_version")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Node版本")
public class FeNodeVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Excel(name = "版本号", width = 20)
    @Schema(description = "Node版本号, 如: v18.19.0")
    private String version;

    @Excel(name = "状态", width = 15)
    @Dict(dicCode = "fe_node_status")
    @Schema(description = "状态: enable=启用, disable=停用")
    private String status;

    @Excel(name = "是否标准版本", width = 15)
    @Schema(description = "是否标准版本: 1=是, 0=否")
    private Integer isStandard;

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
//update-end---author:feplatform ---date:2026-05-22  for:【CICD治理】Node版本实体---
