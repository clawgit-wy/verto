package org.jeecg.modules.feplatform.developer.entity;

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
import java.util.Date;
import java.util.List;

@Data
@TableName(value = "fe_developer", autoResultMap = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "前端开发人员表")
public class FeDeveloper implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "关联系统用户ID")
    private String userId;

    @Excel(name = "姓名", width = 15)
    @Schema(description = "姓名")
    private String realName;

    @Excel(name = "工号", width = 15)
    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "所属团队ID")
    private String teamId;

    @Schema(description = "团队名称")
    @TableField(exist = false)
    private String teamName;

    @Excel(name = "角色", width = 15)
    @Schema(description = "角色: developer/lead/manager")
    private String role;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "技能标签")
    private List<String> skillTags;

    @Excel(name = "状态", width = 15)
    @Schema(description = "状态: active/inactive")
    private String status;

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
}