package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.permission_checker.MeasurableRatingPermissionChecker;
import org.finos.waltz.test_common.helpers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Service
public class MeasurableRatingPermissionCheckerTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private MeasurableRatingPermissionChecker measurableRatingPermissionChecker;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private UserHelper userHelper;

    private final String stem = "mrpc";


    @Test
    public void findMeasurableRatingPermissions() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "rating permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);
        long m2 = measurableHelper.createMeasurable("m2", catId);

        assertThrows(
                IllegalArgumentException.class,
                () -> measurableRatingPermissionChecker.findMeasurableRatingPermissions(null, m1, u1),
                "entity reference cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        Set<Operation> noPermissionsConfigured = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);
        assertEquals(emptySet(), noPermissionsConfigured, "If no permission group involvement returns no permissions"); // created via changelog

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> hasOneOfEditablePermissions = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);
        assertEquals(asSet(Operation.ADD), hasOneOfEditablePermissions, "If has given permission then returns operations for which have the required involvement");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> noPermsWhereNoInvKind = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);
        assertEquals(asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);

        Set<Operation> hasNoInvolvementWithApplication = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appB, m2, u1);
        assertEquals(
                emptySet(),
                hasNoInvolvementWithApplication,
                "User only has permissions on applications they have an involvement with");

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> hasOverrideRoleForCategory = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                hasOverrideRoleForCategory,
                "Returns all edit perms where user has the override role on the category");

        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appB, m2, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all applications");

    }


    @Test
    public void findMeasurableRatingPlannedDecommissionPermissions() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "decomm permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);
        long m2 = measurableHelper.createMeasurable("m2", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);

        assertThrows(
                IllegalArgumentException.class,
                () -> measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, null),
                "username cannot be null");

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        Set<Operation> noPermissionsConfigured = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);
        assertEquals(emptySet(), noPermissionsConfigured, "If no permission group involvement returns no permissions"); // created via changelog

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING_PLANNED_DECOMMISSION,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> hasOneOfEditablePermissions = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);
        assertEquals(asSet(Operation.ADD), hasOneOfEditablePermissions, "If has given permission then returns operations for which have the required involvement");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> noPermsWhereNoInvKind = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);
        assertEquals(asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
        long mrId2 = measurableHelper.createRating(appB, m2);

        Set<Operation> noPermissionsOnAppsNotRelatedTo = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId2, u1);
        assertEquals(
                emptySet(),
                noPermissionsOnAppsNotRelatedTo,
                "User only has permissions on applications they have an involvement with");

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> hasOverrideRoleForCategory = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                hasOverrideRoleForCategory,
                "Returns all edit perms where user has the override role on the category");

        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId2, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all applications");

    }


    @Test
    public void findMeasurableRatingReplacementPermissions() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "replacement permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);
        long m2 = measurableHelper.createMeasurable("m2", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);
        long decommId = measurableHelper.createDecomm(mrId1);

        assertThrows(
                IllegalArgumentException.class,
                () -> measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        Set<Operation> noPermissionsConfigured = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);
        assertEquals(emptySet(), noPermissionsConfigured, "If no permission group involvement returns no permissions"); // created via changelog

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING_REPLACEMENT,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> hasOneOfEditablePermissions = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);
        assertEquals(asSet(Operation.ADD), hasOneOfEditablePermissions, "If has given permission then returns operations for which have the required involvement");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING_REPLACEMENT,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> noPermsWhereNoInvKind = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);
        assertEquals(asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
        long mrId2 = measurableHelper.createRating(appB, m2);
        long decommId2 = measurableHelper.createDecomm(mrId2);

        Set<Operation> noPermissionsOnApplicationsOtherThanThoseUserIsRelatedTo = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId2, u1);
        assertEquals(
                emptySet(),
                noPermissionsOnApplicationsOtherThanThoseUserIsRelatedTo,
                "User only has permissions on applications they have an involvement with");

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> hasOverrideRoleForCategory = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                hasOverrideRoleForCategory,
                "Returns all edit perms where user has the override role on the category");


        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId2, u1);
        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all applications");

    }


    @Test
    public void measurableRatingPermissionsDoNotProvideDecommOrReplacementPermissions() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "cross rating permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);
        long m2 = measurableHelper.createMeasurable("m2", catId);

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.ADD,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> measurableRatingPermissions = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);
        assertEquals(asSet(Operation.ADD), measurableRatingPermissions, "Measurable rating permissions should exist");

        long mrId1 = measurableHelper.createRating(appA, m1);
        Set<Operation> measurableRatingDecommPermissions = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);
        assertEquals(emptySet(), measurableRatingDecommPermissions, "Measurable rating decomm permissions should not exist");

    }

    @Test
    public void findMeasurableRatingPermissionsNullGroupId() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "rating permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        PermissionGroupRecord pg = permissionHelper.createGroup(stem);
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());

        permissionHelper.setupPermissionGroupInvolvement(
                null,
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                mkRef(EntityKind.MEASURABLE_CATEGORY, catId));

        Set<Operation> nullInvolvementGroupGivesAllPermissionsForOperation = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);

        assertEquals(
                asSet(Operation.REMOVE),
                nullInvolvementGroupGivesAllPermissionsForOperation,
                "Null involvement group id gives everyone permissions for ratings without needing override but only for described operations");
    }


    @Test
    public void findMeasurableRatingPermissionsWithReadOnlyRating() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "rating permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        measurableHelper.createRating(appA, m1);
        measurableHelper.updateMeasurableReadOnly(appA, m1);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> readOnlyRatingsShouldNotBeEditable = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);

        assertEquals(
                emptySet(),
                readOnlyRatingsShouldNotBeEditable,
                "Read only rating should restrict should return no permitted operations");
    }

    @Test
    public void findMeasurableRatingDecommPermissionsWithReadOnlyRating() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "decomm permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);
        measurableHelper.updateMeasurableReadOnly(appA, m1);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> readOnlyRatingsShouldNotRestrictEditingDecomms = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);

        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                readOnlyRatingsShouldNotRestrictEditingDecomms,
                "Read only rating should not restrict operations on decomms");
    }

    @Test
    public void findMeasurableRatingReplacementPermissionsWithReadOnlyRating() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "replacement permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);
        long decommId = measurableHelper.createDecomm(mrId1);
        measurableHelper.updateMeasurableReadOnly(appA, m1);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> readOnlyRatingsShouldNotRestrictEditingDecomms = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);

        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                readOnlyRatingsShouldNotRestrictEditingDecomms,
                "Read only rating should not restrict operations on rating replacements");
    }

    @Test
    public void findMeasurableRatingPermissionsWithNonEditableCategory() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "rating permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        measurableHelper.createRating(appA, m1);
        measurableHelper.updateCategoryNotEditable(catId);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> nonEditableCategoryShouldStillAllowRatingsToBeEdited = measurableRatingPermissionChecker.findMeasurableRatingPermissions(appA, m1, u1);

        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                nonEditableCategoryShouldStillAllowRatingsToBeEdited,
                "Non-editable category should still allow ratings to be edited operations");
    }

    @Test
    public void findMeasurableRatingDecommPermissionsWithNonEditableCategory() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "decom permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);
        measurableHelper.updateCategoryNotEditable(catId);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> nonEditableCategoryShouldStillAllowRatingsToBeEdited = measurableRatingPermissionChecker.findMeasurableRatingDecommPermissions(mrId1, u1);

        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                nonEditableCategoryShouldStillAllowRatingsToBeEdited,
                "Non-editable category should still allow decoms to be edited operations");
    }

    @Test
    public void findMeasurableRatingReplacementPermissionsWithNonEditableCategory() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String adminRoleName = mkName(stem, "adminRoleName");

        long catId = measurableHelper.createMeasurableCategory(mkName(stem, "replacement permission checker"), adminRoleName);
        long m1 = measurableHelper.createMeasurable("m1", catId);

        long mrId1 = measurableHelper.createRating(appA, m1);
        long decommId = measurableHelper.createDecomm(mrId1);

        measurableHelper.updateCategoryNotEditable(catId);

        userHelper.createUserWithRoles(u1, adminRoleName);

        Set<Operation> nonEditableCategoryShouldStillAllowRatingsToBeEdited = measurableRatingPermissionChecker.findMeasurableRatingReplacementPermissions(decommId, u1);

        assertEquals(
                asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                nonEditableCategoryShouldStillAllowRatingsToBeEdited,
                "Non-editable category should still allow replacements to be edited operations");
    }
}
