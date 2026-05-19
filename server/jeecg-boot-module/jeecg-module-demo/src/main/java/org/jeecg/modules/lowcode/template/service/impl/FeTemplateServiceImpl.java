package org.jeecg.modules.lowcode.template.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.lowcode.template.entity.FeTemplate;
import org.jeecg.modules.lowcode.template.mapper.FeTemplateMapper;
import org.jeecg.modules.lowcode.template.service.IFeTemplateService;
import org.springframework.stereotype.Service;

@Service
public class FeTemplateServiceImpl extends ServiceImpl<FeTemplateMapper, FeTemplate> implements IFeTemplateService {
}
