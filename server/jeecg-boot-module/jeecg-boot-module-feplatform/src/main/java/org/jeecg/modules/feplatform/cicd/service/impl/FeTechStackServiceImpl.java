package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.feplatform.cicd.entity.FeTechStack;
import org.jeecg.modules.feplatform.cicd.mapper.FeTechStackMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeTechStackService;
import org.springframework.stereotype.Service;

@Service
public class FeTechStackServiceImpl extends ServiceImpl<FeTechStackMapper, FeTechStack> implements IFeTechStackService {
}
