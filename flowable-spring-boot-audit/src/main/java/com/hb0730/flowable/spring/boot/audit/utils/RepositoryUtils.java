package com.hb0730.flowable.spring.boot.audit.utils;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * @author bing_huang
 */
public class RepositoryUtils extends BaseUtils {

    public static Deployment deploymentByKey(String key) {
        return getService().createDeploymentQuery().deploymentKey(key).singleResult();
    }

    public static ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId) {
        return getService().createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
    }

    public static RepositoryService getService(){
        return getService(RepositoryService.class);
    }
}
