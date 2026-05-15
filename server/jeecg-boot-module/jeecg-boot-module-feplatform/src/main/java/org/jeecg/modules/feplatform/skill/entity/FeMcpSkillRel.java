package org.jeecg.modules.feplatform.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("fe_mcp_skill_rel")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "MCP-Skill关联表")
public class FeMcpSkillRel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "MCP Server ID")
    private String mcpId;

    @Schema(description = "Skill ID")
    private String skillId;

    @Schema(description = "排序号")
    private BigDecimal sortNo;
}
