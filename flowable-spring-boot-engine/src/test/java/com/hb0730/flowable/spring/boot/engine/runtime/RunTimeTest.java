package com.hb0730.flowable.spring.boot.engine.runtime;

import cn.hutool.core.img.ImgUtil;
import com.hb0730.flowable.spring.boot.engine.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
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
 * @author bing_huang
 */
@Slf4j
public class RunTimeTest extends BaseTest {
    /**
     * 身份
     */
    @Autowired
    private IdentityService identityService;
    /**
     * 流程
     */
    @Autowired
    private RuntimeService runtimeService;
    /**
     * 部署
     */
    @Autowired
    private RepositoryService repositoryService;
    /**
     * 任务
     */
    @Autowired
    private TaskService taskService;
    /**
     * 历史
     */
    @Autowired
    private HistoryService historyService;
    @Qualifier("processEngine")
    @Autowired
    private ProcessEngine processEngine;
    //ui
    @Autowired
    private ModelService modelService;
    @Autowired
    private ModelRepository modelRepository;

    @Test
    public void queryDeployFlowTest() {
        Deployment approval = repositoryService.createDeploymentQuery()
                .deploymentKey("OrderApproval").singleResult();
        Assert.assertNotNull(approval);
        log.info(approval.getId());
    }

    /**
     * 1. 定义流程
     * 2.部署流程
     */
    @Test
    public void deployFlowTest() {
        List<Model> approval = modelRepository.findByKeyAndType("OrderApproval", 0);
        Assert.assertNotNull(approval);
        Model model = approval.get(0);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        Deployment deploy = repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getKey())
                .tenantId(model.getTenantId())
//                .category()
                .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                .deploy();
        //7048191e-65f8-11eb-badb-60f677c75624
        String id = deploy.getId();
        log.info(id);
    }

    /**
     * 查询流程
     */
    @Test
    public void queryProcessInstanceTest() {
        List<ProcessInstance> approval = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("OrderApproval").list();
        Assert.assertNotNull(approval);
        //[ProcessInstance[7d249527-65fe-11eb-babb-60f677c75624]]
        log.info(approval.toString());
    }

    /**
     * 提交采购订单的审批请求
     * 3. 启动流程
     */
    @Test
    public void startTest() {
        //设置提交用户
//        identityService.setAuthenticatedUserId("Administrator");
        Map<String, Object> map = new HashMap<>();
        //设置提交用户
        map.put("userId", "Administrator");
//        map.put("purchaseOrderId","111111");
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("OrderApproval", "111111", map);
        Assert.assertNotNull(instance);
        // 1.9953e931-65f8-11eb-a705-60f677c75624
        // 2.7d249527-65fe-11eb-babb-60f677c75624
        log.info(instance.getId());
    }

    /**
     * 4. 获取用户任务
     */
    @Test
    public void getTaskByUserIdTest() {
        List<Task> list = taskService.createTaskQuery().taskAssignee("Administrator").orderByTaskAssignee().desc().list();
        Assert.assertNotNull(list);
        //1. [Task[id=9953e931-65f8-11eb-a705-60f677c75624, name=订单审批]]
        //2. [Task[id=7d2c0f3d-65fe-11eb-babb-60f677c75624, name=订单审批]]
        log.info(list.toString());
    }

    /**
     * 4.审批通过
     */
    @Test
    public void auditSuccessTest() {
        String taskId = "99598e87-65f8-11eb-a705-60f677c75624";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (null == task) {
            throw new RuntimeException("task不存在");
        }
        //设置参数
        Map<String, Object> map = new HashMap<>();
        // 在流程图中获取进行处理
        map.put("approved", true);
        taskService.complete(taskId, map);

//        runtimeService.setVariable(taskId, "approved", true);
//        taskService.complete(taskId);
    }

    /**
     * 4. 审批未通过
     */
    @Test
    public void unAuditTest() {
        String taskId = "7d2c0f3d-65fe-11eb-babb-60f677c75624";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (null == task) {
            throw new RuntimeException("task不存在");
        }
        runtimeService.setVariable(task.getExecutionId(), "approved", false);
        taskService.complete(taskId);
    }

    /**
     * 5. 生成流程图
     */
    @Test
    public void getProcessDiagram() {
        // 9953e931-65f8-11eb-a705-60f677c75624
        // 7d249527-65fe-11eb-babb-60f677c75624
        String processId = "7d249527-65fe-11eb-babb-60f677c75624";
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
}
