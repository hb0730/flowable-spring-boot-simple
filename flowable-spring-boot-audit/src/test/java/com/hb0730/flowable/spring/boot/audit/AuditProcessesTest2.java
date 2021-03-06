package com.hb0730.flowable.spring.boot.audit;

import cn.hutool.core.img.ImgUtil;
import com.hb0730.flowable.spring.boot.audit.processes.ProcessesTest;
import com.hb0730.flowable.spring.boot.audit.utils.HistoryUtils;
import com.hb0730.flowable.spring.boot.audit.utils.RepositoryUtils;
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
 * 流程审批-天数大于1且小于2
 *
 * @author bing_huang
 */
@Slf4j
public class AuditProcessesTest2 extends ProcessesTest {
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
     * 2.提交
     */
    @Test
    public void startTest() {
        //指定提交人
        // 指定下一审批人
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        Assert.assertNotNull("流程未部署", deployment);
        ProcessDefinition definition = RepositoryUtils.getProcessDefinitionByDeploymentId(deployment.getId());
        Map<String, Object> params = new HashMap<>();
        // 提交人
        params.put("taskUser", "zhangsan");
        runtimeService.startProcessInstanceById(definition.getId(), "test2020020500003", params);
        setDayParams(4, "zhangsan");
    }

    public void setDayParams(Integer day, String assignee) {
        List<Task> tasks = TaskUtils.findTasksByOrderByTaskCreateTime(assignee);
        Assert.assertNotNull(tasks);
        Task task = tasks.get(0);
        setAuditor(task.getExecutionId(), "directorUser", "director");
        // 局部变量
        taskService.setVariable(task.getId(), "day", day);
        taskService.complete(task.getId());
    }

    public void setAuditor(String executionId, String name, String value) {
        runtimeService.setVariable(executionId, name, value);
    }

    /**
     * 5. 生成流程图
     */
    @Test
    @After
    public void getProcessDiagram() { String processId = "";
        Deployment deployment = RepositoryUtils.deploymentByKey(MODEL_KEY);
        List<HistoricProcessInstance> historicProcessInstances = HistoryUtils.getHistoryByDeploymentId(deployment.getId());
        processId = historicProcessInstances.get(0).getId();
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
    public static class FlowTest extends AuditProcessesTest2 implements Audit {
        /**
         * 经理审批
         */
        @Test
        public void directorAudit() {
            List<Task> tasks = TaskUtils.findTasksByOrderByTaskCreateTime("director");
            Assert.assertNotNull(tasks);
            Task task = tasks.get(0);
            // 设置下一流程审批者
            setAuditor(task.getExecutionId(), "bossUser", "boss");

            Map<String, Object> variables = taskService.getVariables(task.getId());
            variables.put("outcome", "通过");
            audit(task.getId(), variables);
        }

        /**
         * 老板审批
         */
        @Test
        public void bossAudit() {
            List<Task> tasks = TaskUtils.findTasksByOrderByTaskCreateTime("boss");
            Assert.assertNotNull(tasks);
            Task task = tasks.get(0);
            taskService.setVariable(task.getId(), "outcome", "通过");
            audit(task.getId(), null);
        }

        @Override
        public void audit(String taskId, Map<String, Object> params) {
            taskService.complete(taskId, params);
        }
    }

    interface Audit {
        void audit(String taskId, Map<String, Object> params);
    }
}
