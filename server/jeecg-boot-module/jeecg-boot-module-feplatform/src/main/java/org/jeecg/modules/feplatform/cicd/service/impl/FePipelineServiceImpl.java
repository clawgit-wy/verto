package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.cicd.entity.FePipeline;
import org.jeecg.modules.feplatform.cicd.mapper.FePipelineMapper;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineService;
import org.springframework.stereotype.Service;

@Service
public class FePipelineServiceImpl extends ServiceImpl<FePipelineMapper, FePipeline> implements IFePipelineService {
}
