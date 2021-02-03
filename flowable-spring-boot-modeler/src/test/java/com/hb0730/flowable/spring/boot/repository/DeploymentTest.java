package com.hb0730.flowable.spring.boot.repository;

import com.hb0730.flowable.spring.boot.BaseTest;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentQuery;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * 流程部署测试
 *
 * @author bing_huang
 */
public class DeploymentTest extends BaseTest {
    @Qualifier("repositoryService")
    @Autowired
    private RepositoryService service;
    @Autowired
    private ModelService modelService;
    @Autowired
    private ModelRepository modelRepository;

    /**
     * 部署流程，以model方式
     * 由于flowable modeler 与 flowable Engine 使用时存在表差异
     * <ul>
     *     <li>
     *         1. flowable modeler 使用act_de_model
     *     </li>
     *     <li>
     *         3. flowable Engine 使用act_re_model
     *     </li>
     * </ul>
     */
    @Test
    public void createNewDeploymentsTest() {
        List<Model> models = modelRepository.findByKeyAndType("test", 0);
        Model model = models.get(0);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        //发现key name 丢失
//        service
//                .createDeployment()
//                .addBpmnModel("bpmnModelDeploymentTest", bpmnModel)
//                .deploy();

        service.createDeployment()
                .name(model.getName())
                .key(model.getKey())
                .tenantId(model.getTenantId())
//                .category()
                .addBpmnModel(model.getKey() + ".bpmn", bpmnModel)
                .deploy();

    }

    @Test
    public void deploymentsTest() {
        DeploymentQuery query = service.createDeploymentQuery();
        query = query.deploymentKey("test");
        List<Deployment> list = query.list();
        Assert.assertNotNull(list);
    }

    @Test
    public void deleteDeploymentsTest() {
        Deployment deployment = service.createDeploymentQuery().deploymentKey("test").singleResult();
        service.deleteDeployment(deployment.getId());
    }
}
