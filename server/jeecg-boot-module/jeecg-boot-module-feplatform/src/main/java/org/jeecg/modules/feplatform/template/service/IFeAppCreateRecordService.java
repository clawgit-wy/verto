package org.jeecg.modules.feplatform.template.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.feplatform.template.entity.FeAppCreateRecord;

import java.util.Map;

public interface IFeAppCreateRecordService extends IService<FeAppCreateRecord> {

    /**
     * 应用创建向导核心入口。
     * <p>
     * 流程：
     * <ol>
     *   <li>读取模板与模板版本</li>
     *   <li>调用 GitLab API 拉取模板 archive.zip</li>
     *   <li>对模板内容做占位变量替换（Mustache 风格 {{ key }}）</li>
     *   <li>根据 outputType：
     *     <ul>
     *       <li>download：在服务器临时目录生成新 zip，返回本地路径</li>
     *       <li>gitlab：调用 GitLab API 在指定 namespace 下创建新仓库，并提交首批文件</li>
     *     </ul>
     *   </li>
     *   <li>在 fe_application 中写入应用记录，并在 fe_app_create_record 留痕</li>
     * </ol>
     *
     * @param req 向导参数
     * @return 创建记录
     */
    FeAppCreateRecord createAppByWizard(WizardRequest req);

    /**
     * 应用创建向导请求参数
     */
    class WizardRequest {
        public String templateId;
        public String versionId;
        public String appShortName;
        public String appName;
        public String appCode;
        public String ownerId;
        /** download / gitlab */
        public String outputType;
        /** 输出到 GitLab 时：目标 namespace id */
        public Integer gitlabNamespaceId;
        /** 输出到 GitLab 时：目标实例 URL */
        public String gitlabUrl;
        /** 输出到 GitLab 时：访问 token */
        public String gitlabToken;
        /** 占位变量值 */
        public Map<String, Object> params;
    }
}
