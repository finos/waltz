package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.user.ImmutableUpdateRolesCommand;
import org.finos.waltz.model.user.User;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRoleServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private UserHelper helper;

    @Autowired
    private UserRoleService svc;

    @Test
    public void userCanBeCreated() {
        String name = mkName("userCanBeCreated");
        helper.createUser(name);
        User user = svc.getByUserId(name);
        assertNotNull(user, "Expected user");
    }


    @Test
    public void usersCanBeAssignedRoles() {
        String userName = mkName("userCanBeAssignedRoles_User");
        String roleName = mkName("userCanBeAssignedRoles_Role");
        helper.createUser(userName);
        helper.createRole(roleName);

        ImmutableUpdateRolesCommand addRolesCmd = ImmutableUpdateRolesCommand
                .builder()
                .roles(asSet(roleName))
                .comment("test comment")
                .build();

        svc.updateRoles("admin", userName, addRolesCmd);

        User user = svc.getByUserId(userName);

        assertTrue(user.hasRole(roleName), "Expecting user to have role");
        assertFalse(user.hasRole("made up"), "Not expecting user to have made-up role");

        assertTrue(svc.hasRole(userName, asSet(roleName)));
        assertFalse(svc.hasRole(userName, asSet("made up")));

        assertTrue(svc.hasAnyRole(userName, asSet(roleName, "made up")));
        assertFalse(svc.hasAnyRole(userName, asSet("dummy", "made up")));
    }


    @Test
    public void rolesCanBeAddedAndRemoved() {
        String userName = mkName("rolesCanBeAddedAndRemoved_User");
        String role1Name = mkName("rolesCanBeAddedAndRemoved_Role1");
        String role2Name = mkName("rolesCanBeAddedAndRemoved_Role2");

        helper.createUser(userName);
        helper.createRole(role1Name);
        helper.createRole(role2Name);

        ImmutableUpdateRolesCommand addRolesCmd1 = ImmutableUpdateRolesCommand
                .builder()
                .roles(asSet(role1Name))
                .comment("test comment")
                .build();
        svc.updateRoles("admin", userName, addRolesCmd1);
        assertTrue(svc.hasRole(userName, asSet(role1Name)));
        assertFalse(svc.hasRole(userName, asSet(role2Name)));

        ImmutableUpdateRolesCommand addRolesCmd2 = ImmutableUpdateRolesCommand
                .builder()
                .roles(asSet(role1Name, role2Name))
                .comment("test comment")
                .build();
        svc.updateRoles("admin", userName, addRolesCmd2);
        assertTrue(svc.hasRole(userName, asSet(role1Name, role2Name)));

        ImmutableUpdateRolesCommand addRolesCmd3 = ImmutableUpdateRolesCommand
                .builder()
                .roles(asSet(role2Name))
                .comment("test comment")
                .build();
        svc.updateRoles("admin", userName, addRolesCmd3);
        assertTrue(svc.hasRole(userName, asSet(role2Name)));
        assertFalse(svc.hasRole(userName, asSet(role1Name)));

    }



}
