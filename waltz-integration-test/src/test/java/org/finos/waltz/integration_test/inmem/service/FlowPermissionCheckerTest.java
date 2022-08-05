package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.permission.permission_checker.FlowPermissionChecker;
import org.finos.waltz.test_common_again.helpers.*;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.test_common_again.helpers.NameHelper.mkName;
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
    private LogicalFlowHelper flowHelper;

    @Autowired
    private MeasurableHelper measurableHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

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
    }
}
