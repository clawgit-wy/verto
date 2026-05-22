package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.cicd.entity.FeNodeVersion;
import org.jeecg.modules.feplatform.cicd.mapper.FeNodeVersionMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeNodeVersionService;
import org.springframework.stereotype.Service;

@Service
public class FeNodeVersionServiceImpl extends ServiceImpl<FeNodeVersionMapper, FeNodeVersion> implements IFeNodeVersionService {
}
