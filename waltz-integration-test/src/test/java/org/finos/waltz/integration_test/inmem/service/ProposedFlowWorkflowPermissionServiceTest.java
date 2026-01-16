package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.proposed_flow.ProposeFlowPermission;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowPermissionService;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PermissionGroupHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.finos.waltz.model.Operation.APPROVE;
import static org.finos.waltz.model.Operation.REJECT;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.PermissionGroupInvolvement.PERMISSION_GROUP_INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
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

    @Autowired
    private DSLContext dsl;

    private final String stem = "pgst";

    @BeforeEach
    void cleanUp() {

        dsl.deleteFrom(PERMISSION_GROUP_INVOLVEMENT).execute();
        dsl.deleteFrom(INVOLVEMENT).execute();
        dsl.deleteFrom(PERSON).execute();
    }

    @Test
    @DisplayName("Positive: returns permission with correct operation sets")
    void testShouldBuildPermissionWithCorrectOperationSets() {

        // 1. Arrange ----------------------------------------------------------
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        String userName = mkName(stem, "user1");
        Long personA = personHelper.createPerson(userName);

        long involvementKind = involvementHelper.mkInvolvementKind("rel_ab");
        involvementHelper.createInvolvement(personA, involvementKind, appA);
        involvementHelper.createInvolvement(personA, involvementKind, appB);

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, stem);

        permissionHelper.setupPermissionGroupForProposedFlow(appA, ig, stem, APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(appA, ig, stem, REJECT);
        permissionHelper.setupPermissionGroupForProposedFlow(appB, ig, stem, APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(appB, ig, stem, REJECT);

        // 2. Act --------------------------------------------------------------
        ProposeFlowPermission proposeFlowPermission = proposedFlowWorkflowPermissionService.checkUserPermission(userName, appA, appB);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(proposeFlowPermission);
        assertTrue(proposeFlowPermission.sourceApprover().size() > 1);
        assertTrue(proposeFlowPermission.targetApprover().size() > 1);

        Set<Operation> permittedOperationsForSourceApprover = proposeFlowPermission.sourceApprover();
        Set<Operation> permittedOperationsForTargetApprover = proposeFlowPermission.targetApprover();

        assertTrue(permittedOperationsForSourceApprover.contains(APPROVE));
        assertTrue(permittedOperationsForSourceApprover.contains(REJECT));

        assertTrue(permittedOperationsForTargetApprover.contains(APPROVE));
        assertTrue(permittedOperationsForTargetApprover.contains(REJECT));
    }

    @Test
    @DisplayName("Negative: user unknown – both operation sets are empty")
    void negative_unknownUser() {

        // 1. Arrange ----------------------------------------------------------
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        // 2. Act --------------------------------------------------------------
        ProposeFlowPermission proposeFlowPermission = proposedFlowWorkflowPermissionService.checkUserPermission("unknown_user", appA, appB);

        // 3. Assert -----------------------------------------------------------
        Set<Operation> permittedOperationsForSourceApprover = proposeFlowPermission.sourceApprover();
        Set<Operation> permittedOperationsForTargetApprover = proposeFlowPermission.targetApprover();

        assertTrue(permittedOperationsForSourceApprover.isEmpty());
        assertTrue(permittedOperationsForTargetApprover.isEmpty());
    }

    @Test
    @DisplayName("Negative: user no involvement – both operation sets are empty")
    void negative_userHasNoInvolvement() {

        // 1. Arrange ----------------------------------------------------------
        EntityReference appA = appHelper.createNewApp(mkName(stem, "appA"), ouIds.a);
        EntityReference appB = appHelper.createNewApp(mkName(stem, "appB"), ouIds.a);

        String userName = mkName(stem, "user1");
        Long personA = personHelper.createPerson(userName);

        // 2. Act --------------------------------------------------------------
        ProposeFlowPermission proposeFlowPermission = proposedFlowWorkflowPermissionService.checkUserPermission(userName, appA, appB);

        // 3. Assert -----------------------------------------------------------
        Set<Operation> permittedOperationsForSourceApprover = proposeFlowPermission.sourceApprover();
        Set<Operation> permittedOperationsForTargetApprover = proposeFlowPermission.targetApprover();

        assertTrue(permittedOperationsForSourceApprover.isEmpty());
        assertTrue(permittedOperationsForTargetApprover.isEmpty());
    }
}
