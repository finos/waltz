package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.integration_test.inmem.helpers.AppHelper;
import org.finos.waltz.integration_test.inmem.helpers.InvolvementHelper;
import org.finos.waltz.integration_test.inmem.helpers.MeasurableHelper;
import org.finos.waltz.integration_test.inmem.helpers.PersonHelper;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.permission_group.CheckPermissionCommand;
import org.finos.waltz.model.permission_group.ImmutableCheckPermissionCommand;
import org.finos.waltz.model.permission_group.Permission;
import org.finos.waltz.schema.tables.records.*;
import org.finos.waltz.service.permission.PermissionGroupService;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.SetUtilities.*;
import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.tables.InvolvementGroup.INVOLVEMENT_GROUP;
import static org.finos.waltz.schema.tables.InvolvementGroupEntry.INVOLVEMENT_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroup.PERMISSION_GROUP;
import static org.finos.waltz.schema.tables.PermissionGroupEntry.PERMISSION_GROUP_ENTRY;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
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

        setupSpecificPermissionGroupForApp(appB, privKind);

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

        InvolvementGroupRecord ig = setupInvolvementGroup(privKind);
        PermissionGroupRecord pg = setupPermissionGroup(appA, ig);
        setupAttestationPermissionGroupInvolvement(ig.getId(),
                                                   pg.getId(),
                                                   EntityKind.MEASURABLE_RATING,
                                                   EntityKind.APPLICATION,
                                                   mkRef(EntityKind.MEASURABLE_CATEGORY, categoryId));

        assertFalse(permissionGroupService.hasPermission(mkMeasurableCategoryAttestCommand(u1, categoryId, appA)), "u1 should not have access as category 1 is locked down");
        involvementHelper.createInvolvement(u1Id, privKind, appA);
        assertTrue(permissionGroupService.hasPermission(mkMeasurableCategoryAttestCommand(u1, categoryId, appA)), "u1 should now have access");
    }


    @Test
    public void findPermissionsForSubjectKind() {

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        String u1 = mkName(stem, "user1");

        assertEquals(emptySet(),
                permissionGroupService.findPermissionsForParentReference(appA, u1),
                "if person does not exists, should return no permissions");

        Long u1Id = personHelper.createPerson(u1);

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        Set<Permission> permissionsForOperationOnEntityKind = filter(permissionGroupService.findPermissionsForParentReference(appA, u1), p -> p.operation() == Operation.ATTEST);

        assertEquals(
                asSet(EntityKind.LOGICAL_DATA_FLOW, EntityKind.PHYSICAL_FLOW, EntityKind.MEASURABLE_RATING),
                map(permissionsForOperationOnEntityKind, Permission::subjectKind),
                "u1 should have default permissions for all attestation qualifiers");

        setupSpecificPermissionGroupForApp(appA, privKind);

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


    private Long setupSpecificPermissionGroupForApp(EntityReference appRef, Long involvementKindId) {
        InvolvementGroupRecord ig = setupInvolvementGroup(involvementKindId);
        PermissionGroupRecord pg = setupPermissionGroup(appRef, ig);
        return pg.getId();
    }


    private PermissionGroupRecord setupPermissionGroup(EntityReference appRef, InvolvementGroupRecord ig) {
        String pgName = mkName(stem, "_pg");
        PermissionGroupRecord pg = dsl.newRecord(PERMISSION_GROUP);
        pg.setDescription("test group: " + pgName);
        pg.setIsDefault(false);
        pg.setExternalId(pgName);
        pg.setName(pgName);
        pg.setProvenance(mkName(stem, "prov"));
        pg.insert();

        setupPermissionGroupEntry(appRef, pg.getId());
        setupAttestationPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                null);

        return pg;
    }


    private void setupAttestationPermissionGroupInvolvement(Long igId,
                                                            Long pgId,
                                                            EntityKind subjectKind,
                                                            EntityKind parentKind,
                                                            EntityReference qualifierRef) {
        PermissionGroupInvolvementRecord pgi = dsl.newRecord(PERMISSION_GROUP_INVOLVEMENT);
        pgi.setPermissionGroupId(pgId);
        pgi.setInvolvementGroupId(igId);
        pgi.setOperation(Operation.ATTEST.name());
        pgi.setSubjectKind(subjectKind.name());
        pgi.setParentKind(parentKind.name());
        if (qualifierRef != null) {
            pgi.setQualifierId(qualifierRef.id());
            pgi.setQualifierKind(qualifierRef.kind().name());
        }
        pgi.insert();
    }


    private void setupPermissionGroupEntry(EntityReference appRef, Long pgId) {
        PermissionGroupEntryRecord pge = dsl.newRecord(PERMISSION_GROUP_ENTRY);
        pge.setPermissionGroupId(pgId);
        pge.setEntityKind(EntityKind.APPLICATION.name());
        pge.setEntityId(appRef.id());
        pge.insert();
    }


    private InvolvementGroupRecord setupInvolvementGroup(Long involvementKindId) {
        String igName = mkName(stem, "_ig");
        InvolvementGroupRecord ig = dsl.newRecord(INVOLVEMENT_GROUP);
        ig.setName(igName);
        ig.setExternalId(igName);
        ig.setProvenance(mkName(stem, "prov"));
        ig.insert();

        InvolvementGroupEntryRecord ige = dsl.newRecord(INVOLVEMENT_GROUP_ENTRY);
        ige.setInvolvementGroupId(ig.getId());
        ige.setInvolvementKindId(involvementKindId);
        ige.insert();
        return ig;
    }
}
