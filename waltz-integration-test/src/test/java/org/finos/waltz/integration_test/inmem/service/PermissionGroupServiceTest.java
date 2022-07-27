package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.*;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.attestation.UserAttestationPermission;
import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.model.permission_group.ImmutableCheckPermissionCommand;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.junit.jupiter.api.Assertions.*;

@Service
public class PermissionGroupServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private PermissionGroupService permissionGroupService;

    @Autowired
    private PersonHelper personHelper;


    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private InvolvementHelper involvementHelper;


    @Autowired
    private PermissionGroupHelper permissionHelper;

    private final String stem = "pgst";


    @Test
    public void checkBasicPerms() {
        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);
        String u2 = mkName(stem, "user2");
        Long u2Id = personHelper.createPerson(u2);
        String u3 = mkName(stem, "user3");
        Long u3Id = personHelper.createPerson(u3);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        long nonPrivKind = involvementHelper.mkInvolvementKind(mkName(stem, "non_privileged"));

        assertTrue(permissionGroupService.hasPermission(mkLogicalFlowAttestCommand(u1, appA)), "u1 should have access as is open by default");

        permissionHelper.setupSpecificPermissionGroupForApp(appB, privKind, stem);

        involvementHelper.createInvolvement(u1Id, privKind, appB);
        involvementHelper.createInvolvement(u1Id, nonPrivKind, appB);
        involvementHelper.createInvolvement(u2Id, nonPrivKind, appB);

        assertTrue(permissionGroupService.hasPermission(mkLogicalFlowAttestCommand(u1, appB)), "u1 should have access as they have the right priv");
        assertFalse(permissionGroupService.hasPermission(mkLogicalFlowAttestCommand(u2, appB)), "u2 should not have access as they have the wrong priv");
        assertFalse(permissionGroupService.hasPermission(mkLogicalFlowAttestCommand(u3, appB)), "u3 should not have access as they have the no privs");
    }


    @Test
    public void checkQualifierPerms() {
        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);

        long categoryId = measurableHelper.createMeasurableCategory(mkName(stem, "category1"));

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
        PermissionGroupRecord pg = permissionHelper.setupPermissionGroup(appA, ig, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.ATTEST,
                mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));

        assertFalse(permissionGroupService.hasPermission(mkMeasurableCategoryAttestCommand(u1, categoryId, appA)), "u1 should not have access as category 1 is locked down");
        involvementHelper.createInvolvement(u1Id, privKind, appA);
        assertTrue(permissionGroupService.hasPermission(mkMeasurableCategoryAttestCommand(u1, categoryId, appA)), "u1 should now have access");
    }


    @Test
    public void findPermissionsForSubjectKind() {

        String u1 = mkName(stem, "user1");

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);

        assertEquals(emptySet(),
                permissionGroupService.findPermissionsForParentReference(appA, u1),
                "if person does not exists, should return no permissions");

        Long u1Id = personHelper.createPerson(u1);

        assertThrows(UnsupportedOperationException.class,
                () -> permissionGroupService.findPermissionsForParentReference(mkRef(EntityKind.DATA_TYPE, 1L), u1),
                "should throw an exception for invalid permission group parent kinds");

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        Set<Permission> permissionsForOperationOnEntityKind = filter(permissionGroupService.findPermissionsForParentReference(appA, u1), p -> p.operation() == Operation.ATTEST);

        assertEquals(
                asSet(EntityKind.LOGICAL_DATA_FLOW, EntityKind.PHYSICAL_FLOW, EntityKind.MEASURABLE_RATING),
                map(permissionsForOperationOnEntityKind, Permission::subjectKind),
                "u1 should have default permissions for all attestation qualifiers");

        permissionHelper.setupSpecificPermissionGroupForApp(appA, privKind, stem);

        Set<Permission> userHasNoExtraPermissions = permissionGroupService.findPermissionsForParentReference(appA, u1);

        Map<EntityKind, Permission> permissionsByKind = indexBy(userHasNoExtraPermissions, Permission::subjectKind);

        Permission logicalFlowPermission = permissionsByKind.get(EntityKind.LOGICAL_DATA_FLOW);
        assertNull(logicalFlowPermission, "u1 should have no permissions for data flows as doesn't have the all involvements required");

        involvementHelper.createInvolvement(u1Id, privKind, appA);

        Set<Permission> withExtraPermissions = filter(permissionGroupService.findPermissionsForParentReference(appA, u1), p -> p.operation() == Operation.ATTEST);

        assertEquals(
                asSet(EntityKind.LOGICAL_DATA_FLOW, EntityKind.PHYSICAL_FLOW, EntityKind.MEASURABLE_RATING),
                map(withExtraPermissions, Permission::subjectKind),
                "u1 should all permissions as they have the extra involvement required for logical flows");
    }


    @Test
    public void findSupportedMeasurableCategoryAttestations() {

        String u1 = mkName(stem, "user1");

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);

        assertThrows(IllegalArgumentException.class, () -> permissionGroupService.findSupportedMeasurableCategoryAttestations(appA, null), "should throw exception if user id is null");
        assertThrows(IllegalArgumentException.class, () -> permissionGroupService.findSupportedMeasurableCategoryAttestations(null, u1), "should throw exception if parent ref is null");

        Set<UserAttestationPermission> noPermGroup = permissionGroupService
                .findSupportedMeasurableCategoryAttestations(appA, u1);
        assertEquals(emptySet(), noPermGroup, "if no permission group should return no permitted categories to attest");

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));
        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
        PermissionGroupRecord pg = permissionHelper.setupPermissionGroup(appA, ig, stem);
        Long u1Id = personHelper.createPerson(u1);

        Set<UserAttestationPermission> userHasNoInvolvement = permissionGroupService
                .findSupportedMeasurableCategoryAttestations(appA, u1);
        assertEquals(emptySet(), userHasNoInvolvement, "if no involvement with app should return no permitted categories to attest");

        involvementHelper.createInvolvement(u1Id, privKind, appA);

        Set<UserAttestationPermission> userHasNoPermissionForMeasurableCategory = permissionGroupService
                .findSupportedMeasurableCategoryAttestations(appA, u1);
        assertEquals(emptySet(), userHasNoPermissionForMeasurableCategory, "if no permissions to attest measurables should return no permitted categories to attest");

        long categoryId = measurableHelper.createMeasurableCategory(mkName(stem, "category1"));
        long categoryId2 = measurableHelper.createMeasurableCategory(mkName(stem, "category2"));

        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.ATTEST,
                mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));

        long notPermed = involvementHelper.mkInvolvementKind(mkName(stem, "notPrivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(notPermed, stem);

        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.MEASURABLE_RATING,
                EntityKind.APPLICATION,
                Operation.ATTEST,
                mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId2));

        Set<UserAttestationPermission> userHasPermToAttestCategory = permissionGroupService
                .findSupportedMeasurableCategoryAttestations(appA, u1);

        boolean onlyMeasurablePermsReturned = userHasPermToAttestCategory
                .stream()
                .allMatch(d -> d.subjectKind() == EntityKind.MEASURABLE_RATING);

        Set<Long> categoryIds = userHasPermToAttestCategory
                .stream()
                .filter(UserAttestationPermission::hasPermission)
                .map(d -> d.qualifierReference()
                        .map(EntityReference::id)
                        .orElse(null))
                .collect(Collectors.toSet());

        assertTrue(onlyMeasurablePermsReturned, "only permissions to attest measurables should be returned");
        assertEquals(asSet(categoryId), categoryIds, "has permission flag reflects which categories can be attested");
    }


    private CheckPermissionCommand mkLogicalFlowAttestCommand(String u, EntityReference app) {
        return ImmutableCheckPermissionCommand
                .builder()
                .parentEntityRef(app)
                .operation(Operation.ATTEST)
                .subjectKind(EntityKind.LOGICAL_DATA_FLOW)
                .qualifierKind(null)
                .qualifierId(null)
                .user(u)
                .build();
    }


    private CheckPermissionCommand mkMeasurableCategoryAttestCommand(String u, Long categoryId, EntityReference app) {
        return ImmutableCheckPermissionCommand
                .builder()
                .parentEntityRef(app)
                .operation(Operation.ATTEST)
                .subjectKind(EntityKind.MEASURABLE_RATING)
                .qualifierKind(EntityKind.MEASURABLE_CATEGORY)
                .qualifierId(categoryId)
                .user(u)
                .build();
    }

}
