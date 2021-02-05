package com.hb0730.flowable.spring.boot.audit.processes;

import com.hb0730.flowable.spring.boot.audit.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * 部署流程
 *
 * @author bing_huang
 */
@Slf4j
public class ProcessesTest extends BaseTest {
    @Qualifier("repositoryService")
    @Autowired
    private RepositoryService service;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ModelRepository modelRepository;
    protected static final String MODEL_KEY = "leaveExpense";

    @Test
    public void deployProcessesTest() {
        List<Model> leaveExpense = modelRepository.findByKeyAndType(MODEL_KEY, 0);
        Assert.assertNotNull(leaveExpense);
        Model model = leaveExpense.get(0);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        Deployment deploy = service.createDeployment()
                .key(model.getKey())
                .name(model.getName())
                .tenantId(model.getTenantId())
//                .category()
                .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                .deploy();
        log.info(deploy.getId());
    }

}
