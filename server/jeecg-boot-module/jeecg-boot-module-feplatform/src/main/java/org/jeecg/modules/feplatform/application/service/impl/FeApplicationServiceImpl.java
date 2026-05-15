package org.jeecg.modules.feplatform.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.application.entity.FeApplication;
import org.jeecg.modules.feplatform.application.mapper.FeApplicationMapper;
import org.jeecg.modules.feplatform.application.service.IFeApplicationService;
import org.jeecg.modules.feplatform.skill.entity.FeSkill;
import org.jeecg.modules.feplatform.skill.service.IFeSkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeApplicationServiceImpl extends ServiceImpl<FeApplicationMapper, FeApplication> implements IFeApplicationService {

    @Autowired
    private IFeSkillService feSkillService;

    @Override
    public List<FeSkill> getSkillList(String appId) {
        return feSkillService.getEffectiveSkills(appId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSkills(String appId, List<String> skillIds) {
        FeApplication app = getById(appId);
        if (app == null) return;
        List<String> currentIds = app.getSkillIds() != null ? app.getSkillIds() : new ArrayList<>();
        for (String skillId : skillIds) {
            if (!currentIds.contains(skillId)) {
                currentIds.add(skillId);
            }
        }
        app.setSkillIds(currentIds);
        updateById(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindSkills(String appId, List<String> skillIds) {
        FeApplication app = getById(appId);
        if (app == null || app.getSkillIds() == null) return;
        app.getSkillIds().removeAll(skillIds);
        updateById(app);
    }
}
