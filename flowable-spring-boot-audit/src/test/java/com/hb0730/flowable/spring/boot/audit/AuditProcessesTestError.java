package com.hb0730.flowable.spring.boot.audit;

import cn.hutool.core.img.ImgUtil;
import com.hb0730.flowable.spring.boot.audit.processes.ProcessesTest;
import com.hb0730.flowable.spring.boot.audit.utils.HistoryUtils;
import com.hb0730.flowable.spring.boot.audit.utils.RepositoryUtils;
import com.hb0730.flowable.spring.boot.audit.utils.RuntimeUtils;
import com.hb0730.flowable.spring.boot.audit.utils.TaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程审批
 *
 * @author bing_huang
 */
@Slf4j
public class AuditProcessesTestError extends ProcessesTest {
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    protected HistoryService historyService;
    @Qualifier("processEngine")
    @Autowired
    protected ProcessEngine processEngine;


    /**
     * 提交
     */
    @Test
    public void startTest() {
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        ProcessDefinition processDefinition = RepositoryUtils.getProcessDefinitionByDeploymentId(deployment.getId());
        Map<String, Object> params = new HashMap<>();
        params.put("taskUser", "zhangsan");
        params.put("day", 4);
        runtimeService.startProcessInstanceById(processDefinition.getId(), "test1111", params);
        autoAudit();
    }

    protected ProcessInstance processInstanceId() {
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        return runtimeService.createProcessInstanceQuery().deploymentId(deployment.getId()).singleResult();
    }

    protected void autoAudit() {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId().getId()).list();
        Assert.assertNotNull(tasks);
        Task task = tasks.get(0);
        taskService.setVariable(task.getId(), "day", 4);
        taskService.complete(task.getId());
    }

    @Test
    public void tasksTest() {
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        ProcessInstance processInstance = RuntimeUtils.getProcessInstanceIdByDeploymentId(deployment.getId());
        List<Task> task = TaskUtils.getTaskByProcessInstanceId(processInstance.getId());
        //224c1f2b-66c0-11eb-8baa-60f677c75624
        Assert.assertNotNull(task);
    }

    @Test
    public void eventTest() {
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        ProcessInstance processInstance = RuntimeUtils.getProcessInstanceIdByDeploymentId(deployment.getId());
        List<HistoricActivityInstance> instanceId = HistoryUtils.getHistoryByProcessInstanceId(processInstance.getId());
        Assert.assertNotNull(instanceId);
    }

    /**
     * 5. 生成流程图
     */
    @Test
    @After
    public void getProcessDiagram() {
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        ProcessInstance processInstance = RuntimeUtils.getProcessInstanceIdByDeploymentId(deployment.getId());
        String processId = processInstance.getId();
        boolean isFish = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .processInstanceId(processId).count() > 0;
        /**
         * 获得当前活动的节点
         */
        String processDefinitionId = "";
        // 如果流程已经结束，则得到结束节点
        if (isFish) {
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        } else {
            // 如果流程没有结束，则取当前活动节点
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processId)
                    .singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<String>();
        /**
         * 获得活动的节点
         */
        List<HistoricActivityInstance> highLightedActivitList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();
        for (HistoricActivityInstance activityInstance : highLightedActivitList) {
            String activityId = activityInstance.getActivityId();
            highLightedActivitis.add(activityId);
        }
        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration engineConfiguration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engineConfiguration.getProcessDiagramGenerator();

        // 注意：如果是PNG格式那么输出的是背景色是黑色，如果连接线上有字不容易看清楚。可以使用bmp
        InputStream in = diagramGenerator.generateDiagram(
                bpmnModel
                , "png"
                , highLightedActivitis
                , flows
                , "宋体"
                , "宋体"
                , "宋体"
                , engineConfiguration.getClassLoader()
                , 1.0
                , true);

        BufferedImage bufferedImage = ImgUtil.read(in);
        File file = new File("./test.png");
        ImgUtil.write(bufferedImage, file);
    }

    /**
     * 全流程
     */
    public static class FlowTest extends AuditProcessesTestError implements Audit {
        @Test
        public void directorAudit() {
            List<Task> tasks = TaskUtils.getTasksByAssignee("zhangsan");
            Assert.assertNotNull(tasks);
            Task task = tasks.get(0);
            Map<String, Object> params = new HashMap<>();
            params.put("directorUser", "director");
            params.put("outcome", "通过");
            audit(task.getId(), "director", params);
        }

        @Test
        public void bossAudit() {
            List<Task> tasks = TaskUtils.getTasksByAssignee("director");
            Assert.assertNotNull(tasks);
            Task task = tasks.get(0);
            Map<String, Object> params = new HashMap<>();
            params.put("bossUser", "boss");
            params.put("outcome", "通过");
            audit(task.getId(), "boss", params);
        }

        @Override
        public void audit(String taskId, String user, Map<String, Object> params) {
            Task task = TaskUtils.getTaskById(taskId);
            Assert.assertNotNull(taskId);
            Map<String, Object> variables = runtimeService.getVariables(task.getExecutionId());
            Object day = variables.get("day");
            if (null == day || Integer.parseInt(day.toString()) <= 0) {
                taskService.complete(taskId);
            } else {
                taskService.complete(taskId, params);
            }
        }
    }

    interface Audit {
        void audit(String taskId, String user, Map<String, Object> params);
    }
}
