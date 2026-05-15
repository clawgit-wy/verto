package org.jeecg.modules.feplatform.lowcode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Chat2Code对话DTO")
public class Chat2CodeDTO {

    @Schema(description = "用户消息")
    private String message;

    @Schema(description = "选中的Skill ID列表")
    private List<String> skillIds;

    @Schema(description = "关联应用ID")
    private String appId;

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "模型名称(可选)")
    private String modelName;
}
