package com.hb0730.flowable.spring.boot.audit.utils;

import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;

import java.util.List;

/**
 * @author bing_huang
 */
public class HistoryUtils extends BaseUtils {

    public static List<HistoricActivityInstance> getHistoryByProcessInstanceId(String processInstanceId) {
        return getService().createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).list();
    }

    public static List<HistoricProcessInstance> getHistoryByDeploymentId(String deploymentId) {
        return getService().createHistoricProcessInstanceQuery()
                .deploymentId(deploymentId).orderByProcessInstanceStartTime().desc().list();
    }

    public static HistoryService getService() {
        return getService(HistoryService.class);
    }
}
