package org.jeecg.modules.feplatform.skill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "MCP配置导出VO")
public class SkillExportVO {

    @Schema(description = "MCP Server配置")
    private Map<String, Object> mcpServers;

    private SkillExportVO() {
    }

    public static SkillExportVO build(String endpoint, String token, String skills) {
        SkillExportVO vo = new SkillExportVO();
        vo.mcpServers = Map.of(
            "fe-platform", Map.of(
                "command", "npx",
                "args", new String[]{"-y", "@jeecg/mcp-server"},
                "env", Map.of(
                    "MCP_ENDPOINT", endpoint,
                    "MCP_TOKEN", token,
                    "SKILLS", skills
                )
            )
        );
        return vo;
    }
}
