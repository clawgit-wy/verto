package org.jeecg.modules.feplatform.cicd.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.feplatform.cicd.client.JenkinsClient;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;
import org.jeecg.modules.feplatform.cicd.mapper.FeJenkinsInstanceMapper;
import org.jeecg.modules.feplatform.cicd.service.IFeJenkinsInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FeJenkinsInstanceServiceImpl extends ServiceImpl<FeJenkinsInstanceMapper, FeJenkinsInstance>
        implements IFeJenkinsInstanceService {

    @Autowired
    private JenkinsClient jenkinsClient;

    @Override
    public Map<String, Object> testConnection(String id) {
        FeJenkinsInstance instance = getById(id);
        if (instance == null) {
            Map<String, Object> r = new HashMap<>();
            r.put("success", false);
            r.put("error", "Jenkins实例不存在");
            return r;
        }
        return jenkinsClient.ping(instance);
    }
}
