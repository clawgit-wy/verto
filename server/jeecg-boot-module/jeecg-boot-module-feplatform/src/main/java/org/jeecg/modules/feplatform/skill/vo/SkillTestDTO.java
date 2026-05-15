package org.jeecg.modules.feplatform.skill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Skill测试DTO")
public class SkillTestDTO {

    @Schema(description = "Skill ID")
    private String skillId;

    @Schema(description = "输入参数")
    private Map<String, Object> inputParams;

    @Schema(description = "用户消息")
    private String userMessage;

    @Schema(description = "模型名称(可选)")
    private String modelName;
}
