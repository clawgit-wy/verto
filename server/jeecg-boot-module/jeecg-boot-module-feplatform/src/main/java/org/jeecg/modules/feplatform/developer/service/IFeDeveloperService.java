package org.jeecg.modules.feplatform.developer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.developer.entity.FeDeveloper;

import java.util.List;

/**
 * @Description: 前端开发人员表
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
public interface IFeDeveloperService extends IService<FeDeveloper> {

    /**
     * 根据团队ID查询开发人员列表
     */
    List<FeDeveloper> getByTeamId(String teamId);
}