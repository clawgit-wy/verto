package org.jeecg.modules.feplatform.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;

import java.util.List;

public interface IFeSkillService extends IService<FeSkill> {

    List<FeSkill> listByCategory(String category);

    List<FeSkill> listByAppId(String appId);

    List<FeSkill> getEffectiveSkills(String appId);
}
