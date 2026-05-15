package org.jeecg.modules.feplatform.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.skill.entity.FeMcpSkillRel;
import org.jeecg.modules.feplatform.skill.mapper.FeMcpSkillRelMapper;
import org.jeecg.modules.feplatform.skill.service.IFeMcpSkillRelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FeMcpSkillRelServiceImpl extends ServiceImpl<FeMcpSkillRelMapper, FeMcpSkillRel> implements IFeMcpSkillRelService {

    @Override
    public List<FeMcpSkillRel> listByMcpId(String mcpId) {
        LambdaQueryWrapper<FeMcpSkillRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeMcpSkillRel::getMcpId, mcpId)
               .orderByAsc(FeMcpSkillRel::getSortNo);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindSkills(String mcpId, List<String> skillIds) {
        List<FeMcpSkillRel> existingRels = listByMcpId(mcpId);
        List<String> existingSkillIds = existingRels.stream()
                .map(FeMcpSkillRel::getSkillId)
                .collect(Collectors.toList());
        BigDecimal maxSortNo = existingRels.stream()
                .map(FeMcpSkillRel::getSortNo)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        for (String skillId : skillIds) {
            if (!existingSkillIds.contains(skillId)) {
                FeMcpSkillRel rel = new FeMcpSkillRel();
                rel.setId(UUID.randomUUID().toString());
                rel.setMcpId(mcpId);
                rel.setSkillId(skillId);
                rel.setSortNo(maxSortNo.add(BigDecimal.ONE));
                save(rel);
                maxSortNo = rel.getSortNo();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindSkills(String mcpId, List<String> skillIds) {
        LambdaQueryWrapper<FeMcpSkillRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeMcpSkillRel::getMcpId, mcpId)
               .in(FeMcpSkillRel::getSkillId, skillIds);
        remove(wrapper);
    }
}
