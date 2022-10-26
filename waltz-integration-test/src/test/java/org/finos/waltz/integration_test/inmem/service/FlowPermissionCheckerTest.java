package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.permission_checker.FlowPermissionChecker;
import org.finos.waltz.test_common.helpers.*;
import org.jooq.DSLContext;
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
public class FlowPermissionCheckerTest extends BaseInMemoryIntegrationTest {

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private FlowPermissionChecker flowPermissionChecker;

    @Autowired
    private PersonHelper personHelper;


    @Autowired
    private PhysicalSpecHelper specHelper;

    @Autowired
    private LogicalFlowHelper flowHelper;

    @Autowired
    private PhysicalFlowHelper physicalFlowHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private UserHelper userHelper;

    private final String stem = "fpc";


    @Test
    public void findPermissionsForFlow() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForFlow(null, u1),
                "flow id cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForFlow(1L, null),
                "username cannot be null");

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
        EntityReference appC = appHelper.createNewApp(mkName(stem, "appC"), ouIds.a1);

        LogicalFlow flowAB = flowHelper.createLogicalFlow(appA, appB);
        LogicalFlow flowBC = flowHelper.createLogicalFlow(appB, appC);

        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
        PermissionGroupRecord pg = permissionHelper.createGroup(stem);

        Set<Operation> takesDefaults = flowPermissionChecker.findPermissionsForFlow(flowAB.id().get(), u1);
        assertEquals(asSet(Operation.ATTEST), takesDefaults, "If no specified override, takes default permission group"); // created via changelog

        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appB, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appC, pg.getId());

        involvementHelper.createInvolvement(u1Id, privKind, appA);

        Set<Operation> noPermEntries = flowPermissionChecker.findPermissionsForFlow(flowAB.id().get(), u1);
        assertEquals(emptySet(), noPermEntries, "If given override group, should not take any defaults");

        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                Operation.ADD,
                null);

        Set<Operation> hasInv = flowPermissionChecker.findPermissionsForFlow(flowAB.id().get(), u1);
        Set<Operation> noInv = flowPermissionChecker.findPermissionsForFlow(flowBC.id().get(), u1);
        assertEquals(
                asSet(Operation.ADD),
                hasInv,
                "takes override from permission group entry only");
        assertEquals(emptySet(), noInv, "returns no permission if has no involvement with flow");

        flowHelper.makeReadOnly(flowAB.id().get());
        Set<Operation> readOnlyFlow = flowPermissionChecker.findPermissionsForFlow(flowAB.id().get(), u1);
        assertEquals(
                emptySet(),
                readOnlyFlow,
                "users should not be able to edit read only flows");
    }

    @Test
    public void findPermissionsForFlowDecorator() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
        EntityReference appC = appHelper.createNewApp(mkName(stem, "appC"), ouIds.a1);

        LogicalFlow flowAB = flowHelper.createLogicalFlow(appA, appB);
        LogicalFlow flowBC = flowHelper.createLogicalFlow(appB, appC);

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForDecorator(null, u1),
                "entity reference cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForDecorator(flowAB.entityReference(), null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
        PermissionGroupRecord pg = permissionHelper.createGroup(stem);

        // all aps must be in isolated group otherwise default permissions inherited too
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appB, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appC, pg.getId());

        Set<Operation> noPermissionEntries = flowPermissionChecker.findPermissionsForDecorator(flowAB.entityReference(), u1);
        assertEquals(emptySet(), noPermissionEntries, "If no permission group involvement then no permissions");


        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                Operation.ADD,
                null);

        Set<Operation> hasOneOfEditablePermissions = flowPermissionChecker.findPermissionsForDecorator(flowAB.entityReference(), u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), hasOneOfEditablePermissions, "Flow permissions return id permission on either source or target");

        Set<Operation> hasNoInvolvementForEditablePermissions = flowPermissionChecker.findPermissionsForDecorator(flowBC.entityReference(), u1);
        assertEquals(emptySet(), hasNoInvolvementForEditablePermissions, "Should be no flow permissions if user is not associated to source or target and has no override");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.LOGICAL_DATA_FLOW,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                null);

        Set<Operation> noPermsWhereNoInvKind = flowPermissionChecker.findPermissionsForDecorator(flowAB.entityReference(), u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        userHelper.createUserWithSystemRoles(u1, SetUtilities.asSet(SystemRole.LOGICAL_DATA_FLOW_EDITOR));

        Set<Operation> ifOverrideRoleThenAllEditPermsReturned = flowPermissionChecker.findPermissionsForDecorator(flowAB.entityReference(), u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                ifOverrideRoleThenAllEditPermsReturned,
                "Returns all edit perms where user has the override role for logical flows");

        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = flowPermissionChecker.findPermissionsForDecorator(flowBC.entityReference(), u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all flow decorators");
    }


    @Test
    public void findPermissionsForSpecDecorator() {

        String u1 = mkName(stem, "user1");
        Long u1Id = personHelper.createPerson(u1);

        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.b);
        EntityReference appC = appHelper.createNewApp(mkName(stem, "appC"), ouIds.a1);

        LogicalFlow flowAB = flowHelper.createLogicalFlow(appA, appB);
        LogicalFlow flowBC = flowHelper.createLogicalFlow(appB, appC);

        Long specId = specHelper.createPhysicalSpec(appA, mkName(stem, "perms"));
        Long specId2 = specHelper.createPhysicalSpec(appB, mkName(stem, "perms"));
        PhysicalFlowCreateCommandResponse createFlowResp = physicalFlowHelper.createPhysicalFlow(flowAB.entityReference().id(), specId, mkName(stem, "spec perms"));
        EntityReference specRef = mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId);
        EntityReference specRef2 = mkRef(EntityKind.PHYSICAL_SPECIFICATION, specId2);

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForDecorator(null, u1),
                "entity reference cannot be null");

        assertThrows(
                IllegalArgumentException.class,
                () -> flowPermissionChecker.findPermissionsForDecorator(specRef, null),
                "username cannot be null");


        long privKind = involvementHelper.mkInvolvementKind(mkName(stem, "privileged"));

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(privKind, stem);
        PermissionGroupRecord pg = permissionHelper.createGroup(stem);

        // all aps must be in isolated group otherwise default permissions inherited too
        permissionHelper.setupPermissionGroupEntry(appA, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appB, pg.getId());
        permissionHelper.setupPermissionGroupEntry(appC, pg.getId());

        Set<Operation> noPermissionEntries = flowPermissionChecker.findPermissionsForDecorator(specRef, u1);
        assertEquals(emptySet(), noPermissionEntries, "If no permission group involvement then no permissions");

        involvementHelper.createInvolvement(u1Id, privKind, appA);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.PHYSICAL_SPECIFICATION,
                EntityKind.APPLICATION,
                Operation.ADD,
                null);


        Set<Operation> hasOneOfEditablePermissions = flowPermissionChecker.findPermissionsForDecorator(specRef, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), hasOneOfEditablePermissions, "Flow permissions return permission for source ref");

        involvementHelper.createInvolvement(u1Id, privKind, appC);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.PHYSICAL_SPECIFICATION,
                EntityKind.APPLICATION,
                Operation.UPDATE,
                null);

        Set<Operation> specIsOwnedByTarget = flowPermissionChecker.findPermissionsForDecorator(specRef2, u1);
        assertEquals(SetUtilities.asSet(Operation.UPDATE), specIsOwnedByTarget, "Spec permissions should be inherited from spec owner involvement regardless of whether this is source or target");

        long unprivKind = involvementHelper.mkInvolvementKind(mkName(stem, "unprivileged"));
        InvolvementGroupRecord ig2 = permissionHelper.setupInvolvementGroup(unprivKind, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig2.getId(),
                pg.getId(),
                EntityKind.PHYSICAL_SPECIFICATION,
                EntityKind.APPLICATION,
                Operation.REMOVE,
                null);

        Set<Operation> noPermsWhereNoInvKind = flowPermissionChecker.findPermissionsForDecorator(specRef, u1);
        assertEquals(SetUtilities.asSet(Operation.ADD), noPermsWhereNoInvKind, "Doesn't return perms for operations where user lacks the required inv kind");

        userHelper.createUserWithSystemRoles(u1, SetUtilities.asSet(SystemRole.PHYSICAL_SPECIFICATION_EDITOR));

        Set<Operation> ifOverrideRoleThenAllEditPermsReturned = flowPermissionChecker.findPermissionsForDecorator(specRef, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                ifOverrideRoleThenAllEditPermsReturned,
                "Returns all edit perms where user has the override role for physical specs");

        Set<Operation> overRideRoleGivesAllEditPermsOnAnyApp = flowPermissionChecker.findPermissionsForDecorator(specRef2, u1);
        assertEquals(
                SetUtilities.asSet(Operation.ADD, Operation.UPDATE, Operation.REMOVE),
                overRideRoleGivesAllEditPermsOnAnyApp,
                "Override role provides edit permissions on all spec decorators");
    }

    // worth testing whether a null ig gives fujll permissions!!
    // worth testing that a spec not invovled in any flows is updatable based upon just override
}
