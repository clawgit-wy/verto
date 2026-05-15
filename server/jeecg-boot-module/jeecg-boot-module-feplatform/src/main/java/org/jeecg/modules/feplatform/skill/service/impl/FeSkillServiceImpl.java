package org.jeecg.modules.feplatform.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;
import org.jeecg.modules.feplatform.skill.mapper.FeSkillMapper;
import org.jeecg.modules.feplatform.skill.service.IFeSkillService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeSkillServiceImpl extends ServiceImpl<FeSkillMapper, FeSkill> implements IFeSkillService {

    @Override
    public List<FeSkill> listByCategory(String category) {
        LambdaQueryWrapper<FeSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeSkill::getCategory, category)
               .eq(FeSkill::getStatus, "enable")
               .orderByAsc(FeSkill::getSortNo);
        return list(wrapper);
    }

    @Override
    public List<FeSkill> listByAppId(String appId) {
        LambdaQueryWrapper<FeSkill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeSkill::getAppId, appId)
               .eq(FeSkill::getStatus, "enable")
               .orderByAsc(FeSkill::getSortNo);
        return list(wrapper);
    }

    @Override
    public List<FeSkill> getEffectiveSkills(String appId) {
        List<FeSkill> skills = new ArrayList<>();
        if (appId != null && !appId.isEmpty()) {
            skills.addAll(listByAppId(appId));
        }
        skills.addAll(listByCategory("business"));
        skills.addAll(listByCategory("official"));
        return deduplicateByCode(skills);
    }

    private List<FeSkill> deduplicateByCode(List<FeSkill> skills) {
        Map<String, FeSkill> map = new LinkedHashMap<>();
        for (FeSkill skill : skills) {
            map.putIfAbsent(skill.getCode(), skill);
        }
        return new ArrayList<>(map.values());
    }
}
