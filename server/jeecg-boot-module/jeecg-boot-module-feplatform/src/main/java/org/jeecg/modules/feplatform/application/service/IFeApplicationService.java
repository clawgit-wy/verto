package org.jeecg.modules.feplatform.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.application.entity.FeApplication;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;

import java.util.List;

public interface IFeApplicationService extends IService<FeApplication> {

    List<FeSkill> getSkillList(String appId);

    void bindSkills(String appId, List<String> skillIds);

    void unbindSkills(String appId, List<String> skillIds);
}
