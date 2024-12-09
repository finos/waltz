package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.bulk_upload.BulkUploadMode;
import org.finos.waltz.model.settings.ImmutableSetting;
import org.finos.waltz.model.user.BulkUserOperationRowPreview;
import org.finos.waltz.model.user.ImmutableUpdateRolesCommand;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.model.user.User;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.finos.waltz.test_common.helpers.UserHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class UserRoleServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private UserHelper helper;

    @Autowired
    private UserRoleService svc;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private PersonHelper personHelper;



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

    @Test
    public void canFindAllUsersForAGivenRole() {
        String user1Name = mkName("canFindAllUsersForAGivenRole_User1");
        String user2Name = mkName("canFindAllUsersForAGivenRole_User2");
        String roleName = mkName("canFindAllUsersForAGivenRole_Role1");

        helper.createUser(user1Name);
        helper.createUser(user2Name);
        Long roleId = helper.createRole(roleName);

        ImmutableUpdateRolesCommand addRolesCmd = ImmutableUpdateRolesCommand
                .builder()
                .roles(asSet(roleName))
                .comment("test comment")
                .build();
        svc.updateRoles("admin", user1Name, addRolesCmd);
        svc.updateRoles("admin", user2Name, addRolesCmd);

        Set<User> users = svc.findUsersForRole(roleId);

        assertEquals(
                "Expect (only) user 1 and 2 to be associated to role ("+roleId+")",
                SetUtilities.asSet(user1Name, user2Name),
                SetUtilities.map(users, User::userName));

    }

    @Test
    public void adminCannotChangeOwnRolesIndividuallyIfFECEnabled() {
        // FEC = Four Eye Check
        String stem = "adminCannotChangeOwnRolesIfFECEnabled";
        String username = mkName(stem + "_user");
        String randomRole = mkName(stem + "_random_role");

        helper.createUser(username);
        personHelper.createPerson(username);

        svc.updateRoles(username, username, ImmutableUpdateRolesCommand.builder()
                .addRoles(SystemRole.ADMIN.name())
                .comment("cmnt")
                .build());

        helper.createRole(randomRole);

        // creating the four-eye-check setting with true as value
        settingsService.create(ImmutableSetting.builder()
                .name("feature.user-roles.four-eye-check")
                .description("desc")
                .value("true")
                .restricted(false)
                .build());

        try {
            svc.updateRoles(username, username, ImmutableUpdateRolesCommand.builder()
                    .addRoles(randomRole)
                    .comment("should_not_add")
                    .build());

        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void adminCannotChangeOwnRolesButCanOthersRolesInBulkIfFECEnabled() {
        // FEC = Four Eye Check
        String stem = "adminCannotChangeOwnRolesButCanOthersRolesInBulkIfFECEnabled";
        String username = mkName(stem + "_user");
        String randomRole = mkName(stem + "_random_role");

        String bulkUploadString = format("username, role, comment\n" +
            "%s, %s, comment\n" +
            "%s, %s, comment2",
            username, randomRole, username + "2", randomRole);

        String[] lines = bulkUploadString.split("\\R");

        helper.createUser(username);
        personHelper.createPerson(username);
        helper.createUser(username + "2");
        personHelper.createPerson(username + "2");

        svc.updateRoles(username, username, ImmutableUpdateRolesCommand.builder()
                .addRoles(SystemRole.ADMIN.name())
                .comment("cmnt")
                .build());

        helper.createRole(randomRole);

        // creating the four-eye-check setting with true as value
        settingsService.create(ImmutableSetting.builder()
                .name("feature.user-roles.four-eye-check")
                .description("desc")
                .value("true")
                .restricted(false)
                .build());


        svc.bulkUpload(BulkUploadMode.ADD_ONLY, ListUtilities.asList(lines), username);
        assertFalse(svc.hasRole(username, randomRole));
        assertTrue(svc.hasRole(username + "2", randomRole));
    }

}
