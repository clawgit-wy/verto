package org.jeecg.modules.feplatform.cicd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.cicd.entity.FeJenkinsInstance;

import java.util.Map;

public interface IFeJenkinsInstanceService extends IService<FeJenkinsInstance> {

    /**
     * 调 Jenkins /api/json 测试连通性。
     */
    Map<String, Object> testConnection(String id);
}
