package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;
import org.jeecg.modules.feplatform.cicd.mapper.FeJenkinsInstanceMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeJenkinsInstanceService;
import org.springframework.stereotype.Service;

@Service
public class FeJenkinsInstanceServiceImpl extends ServiceImpl<FeJenkinsInstanceMapper, FeJenkinsInstance> implements IFeJenkinsInstanceService {
}
