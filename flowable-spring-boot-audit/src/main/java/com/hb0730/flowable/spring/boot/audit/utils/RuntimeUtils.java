package com.hb0730.flowable.spring.boot.audit.utils;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.List;

/**
 * @author bing_huang
 */
public class RuntimeUtils extends BaseUtils {


    public static ProcessInstance getProcessInstanceIdByDeploymentId(String deploymentId) {
        return getService().createProcessInstanceQuery().deploymentId(deploymentId).singleResult();
    }

    public static List<ProcessInstance> getProcessInstancesByDeploymentId(String deploymentId) {
        return getService().createProcessInstanceQuery()
                .deploymentId(deploymentId)
                .orderByStartTime().list();
    }

    public static RuntimeService getService() {
        return getService(RuntimeService.class);
    }
}
