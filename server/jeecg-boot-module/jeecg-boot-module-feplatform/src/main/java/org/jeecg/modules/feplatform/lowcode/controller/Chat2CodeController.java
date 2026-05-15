package org.jeecg.modules.feplatform.lowcode.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.feplatform.lowcode.dto.Chat2CodeDTO;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;
import org.jeecg.modules.feplatform.skill.service.IFeSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Chat2Code低代码对话")
@RestController
@RequestMapping("/feplatform/lowcode")
@Slf4j
public class Chat2CodeController {

    @Autowired
    private IFeSkillService feSkillService;

    @Operation(summary = "Chat2Code-对话接口")
    @PostMapping(value = "/chat2code")
    public Result<?> chat2code(@RequestBody Chat2CodeDTO dto, HttpServletResponse response) {
        List<FeSkill> skills;
        if (dto.getSkillIds() != null && !dto.getSkillIds().isEmpty()) {
            skills = feSkillService.listByIds(dto.getSkillIds());
        } else if (dto.getAppId() != null) {
            skills = feSkillService.getEffectiveSkills(dto.getAppId());
        } else {
            skills = feSkillService.listByCategory("official");
        }
        List<FeSkill> enabledSkills = skills.stream()
                .filter(s -> "enable".equals(s.getStatus()))
                .collect(Collectors.toList());

        String systemPrompt = enabledSkills.stream()
                .map(FeSkill::getPromptTemplate)
                .collect(Collectors.joining("\n\n---\n\n"));

        log.info("[Chat2Code] 使用{}个Skill, systemPrompt长度: {}", enabledSkills.size(), systemPrompt.length());

        return Result.OK("Chat2Code接口已就绪，等待接入AI对话引擎", enabledSkills);
    }

    @Operation(summary = "Chat2Code-获取可用Skill列表")
    @GetMapping(value = "/availableSkills")
    public Result<List<FeSkill>> availableSkills(@RequestParam(required = false) String appId) {
        List<FeSkill> skills;
        if (appId != null && !appId.isEmpty()) {
            skills = feSkillService.getEffectiveSkills(appId);
        } else {
            skills = feSkillService.list();
        }
        return Result.OK(skills);
    }
}
