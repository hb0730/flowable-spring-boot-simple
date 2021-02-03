package com.hb0730.flowable.spring.boot.modeler.configuration;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.*;
import org.flowable.idm.api.IdmEngineConfigurationApi;
import org.flowable.idm.api.IdmIdentityService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author bing_huang
 */
@Configuration
@RequiredArgsConstructor
public class ProcessEngineConfiguration {

    /**
     * 流程定义与流程部署
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link RepositoryService} 流程定义与流程部署
     */
    @Bean
    @ConditionalOnMissingBean
    public RepositoryService repositoryService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    /**
     * 流程实例与流程的执行等操作
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link RuntimeService}
     */
    @Bean
    @ConditionalOnMissingBean
    public RuntimeService runtimeService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    /**
     * 任务等操作
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link TaskService}
     */
    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    /**
     * 历史数据
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link HistoryService}
     */
    @Bean
    @ConditionalOnMissingBean
    public HistoryService historyService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    /**
     * form操作
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link FormService}
     */
    @Bean
    @ConditionalOnMissingBean
    public FormService formService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getFormService();
    }

    /**
     * 数据库表操作
     *
     * @param processEngine flowable 公开的访问方式
     * @return {@link ManagementService}
     */
    @Bean
    @ConditionalOnMissingBean
    public ManagementService managementService(@Qualifier("processEngine") ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }


    /**
     * 用户，用户组操作，<br> {@link IdmIdentityService} 比{@link IdentityService} 多出对用户权限，用户组权限绑定
     *
     * @param configuration {@link IdmEngineConfigurationApi} idm流程
     * @return {@link IdmIdentityService}身份认证操作类
     */
    @Bean
    @ConditionalOnMissingBean
    public IdmIdentityService identityService(IdmEngineConfigurationApi configuration) {
        return configuration.getIdmIdentityService();
    }
}
