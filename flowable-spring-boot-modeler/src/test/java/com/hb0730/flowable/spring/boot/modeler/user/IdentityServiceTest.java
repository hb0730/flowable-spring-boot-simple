package com.hb0730.flowable.spring.boot.modeler.user;

import com.hb0730.flowable.spring.boot.BaseTest;
import org.flowable.idm.api.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户与用户组,权限测试
 *
 * @author bing_huang
 */
public class IdentityServiceTest extends BaseTest {
    @Autowired
    private IdmIdentityService idmIdentityService;

    @Test
    public void deleteUserGroupTest() {
        idmIdentityService.deleteGroup("1");
    }

    @Test
    public void createUserGroupTest() {
        Group group = idmIdentityService.newGroup("Administrator");
        group.setName("超级管理员");
        idmIdentityService.saveGroup(group);
    }

    @Test
    public void createUserTest() {
        User user = idmIdentityService.newUser("Administrator");
        user.setDisplayName("测试用户");
        idmIdentityService.saveUser(user);
        idmIdentityService.createMembership("Administrator", "Administrator");
    }

    @Test
    public void createPrivTest() {
        Privilege privilege = idmIdentityService.createPrivilege("flow:query");
        idmIdentityService.addUserPrivilegeMapping(privilege.getId(), "Administrator");
    }

    @Test
    public void queryPrivilegeTest() {
        PrivilegeQuery query = idmIdentityService.createPrivilegeQuery();
        PrivilegeQuery privilegeQuery = query.privilegeName("flow:query");
        Privilege privilege = privilegeQuery.singleResult();
        idmIdentityService.getPrivilegeMappingsByPrivilegeId(privilege.getId());
    }
}
