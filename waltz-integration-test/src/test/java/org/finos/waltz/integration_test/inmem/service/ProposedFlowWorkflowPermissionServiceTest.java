package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.proposed_flow.ProposeFlowPermission;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.schema.tables.records.PermissionGroupRecord;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowPermissionService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PermissionGroupHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposedFlowWorkflowPermissionServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    ProposedFlowWorkflowPermissionService proposedFlowWorkflowPermissionService;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private AppHelper appHelper;

    private final String stem = "pgst";

    @Test
    void testShouldBuildPermissionWithCorrectOperationSets() {

        // 1. Arrange ----------------------------------------------------------
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        String userName = mkName(stem, "user1");
        Long personA = personHelper.createPerson(userName);

        long involvementKind = involvementHelper.mkInvolvementKind("rel_a");
        involvementHelper.createInvolvement(personA, involvementKind, appA);
        involvementHelper.createInvolvement(personA, involvementKind, appB);

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, stem);
        PermissionGroupRecord pg = permissionHelper.setupPermissionGroupForProposedFlow(appA, ig, stem);
        PermissionGroupRecord pg_1 = permissionHelper.setupPermissionGroupForProposedFlow(appB, ig, stem);
        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.PROPOSED_FLOW,
                EntityKind.APPLICATION,
                Operation.APPROVE,
                null);

        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg.getId(),
                EntityKind.PROPOSED_FLOW,
                EntityKind.APPLICATION,
                Operation.REJECT,
                null);

        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg_1.getId(),
                EntityKind.PROPOSED_FLOW,
                EntityKind.APPLICATION,
                Operation.APPROVE,
                null);

        permissionHelper.setupPermissionGroupInvolvement(
                ig.getId(),
                pg_1.getId(),
                EntityKind.PROPOSED_FLOW,
                EntityKind.APPLICATION,
                Operation.REJECT,
                null);

        // 2. Act --------------------------------------------------------------
        ProposeFlowPermission proposeFlowPermission = proposedFlowWorkflowPermissionService.checkUserPermission(userName, appA, appB);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(proposeFlowPermission);
        assertTrue(proposeFlowPermission.sourceApprover().size() > 1);
        assertTrue(proposeFlowPermission.targetApprover().size() > 1);
    }
}
