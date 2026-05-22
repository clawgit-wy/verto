package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.cicd.entity.FePipelineBuild;
import org.jeecg.modules.feplatform.cicd.mapper.FePipelineBuildMapper;
import org.jeecg.modules.feplatform.cicd.service.IFePipelineBuildService;
import org.springframework.stereotype.Service;

@Service
public class FePipelineBuildServiceImpl extends ServiceImpl<FePipelineBuildMapper, FePipelineBuild> implements IFePipelineBuildService {
}
