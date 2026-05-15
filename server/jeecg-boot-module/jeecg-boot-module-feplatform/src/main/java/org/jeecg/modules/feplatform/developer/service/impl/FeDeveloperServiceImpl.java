package org.jeecg.modules.feplatform.developer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.developer.entity.FeDeveloper;
import org.jeecg.modules.feplatform.developer.mapper.FeDeveloperMapper;
import org.jeecg.modules.feplatform.developer.service.IFeDeveloperService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 前端开发人员表
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Service
public class FeDeveloperServiceImpl extends ServiceImpl<FeDeveloperMapper, FeDeveloper> implements IFeDeveloperService {

    @Override
    public List<FeDeveloper> getByTeamId(String teamId) {
        LambdaQueryWrapper<FeDeveloper> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeDeveloper::getTeamId, teamId)
                    .eq(FeDeveloper::getDelFlag, 0);
        return this.list(queryWrapper);
    }
}