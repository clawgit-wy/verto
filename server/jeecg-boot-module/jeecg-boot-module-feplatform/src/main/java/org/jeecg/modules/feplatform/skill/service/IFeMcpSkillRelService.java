package org.jeecg.modules.feplatform.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.skill.entity.FeMcpSkillRel;

import java.util.List;

public interface IFeMcpSkillRelService extends IService<FeMcpSkillRel> {

    List<FeMcpSkillRel> listByMcpId(String mcpId);

    void bindSkills(String mcpId, List<String> skillIds);

    void unbindSkills(String mcpId, List<String> skillIds);
}
