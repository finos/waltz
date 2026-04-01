/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.integration_test.inmem.service;

import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.involvement.InvolvementDao;
import org.finos.waltz.data.involvement_kind.InvolvementKindDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_flow.DataFlowService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.finos.waltz.test_common.helpers.*;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.data.proposed_flow.ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW;
import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.Operation.REJECT;
import static org.finos.waltz.model.proposed_flow.ProposalType.CREATE;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.FULLY_APPROVED;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.SOURCE_APPROVED;
import static org.finos.waltz.schema.Tables.ENTITY_WORKFLOW_STATE;
import static org.finos.waltz.schema.Tables.LOGICAL_FLOW;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.APPROVE;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.*;

public class ProposedFlowWorkflowServiceTest extends BaseInMemoryIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowWorkflowServiceTest.class);

    private final String USER_NAME = "testUser";

    @Autowired
    private ProposedFlowWorkflowService proposedFlowWorkflowService;

    @Autowired
    private ProposedFlowDao proposedFlowDao;

    @Autowired
    private LogicalFlowDao logicalFlowDao;

    @Autowired
    private PhysicalSpecificationDao physicalSpecificationDao;

    @Autowired
    private EntityWorkflowStateDao entityWorkflowStateDao;

    @Autowired
    private EntityWorkflowService entityWorkflowService;

    @Autowired
    private ChangeLogService changeLogService;

    @Autowired
    private PhysicalFlowDao physicalFlowDao;

    @Autowired
    private InvolvementDao involvementDao;

    @Autowired
    private PersonDao personDao;

    @Autowired
    private InvolvementKindDao involvementKindDao;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private AppHelper appHelper;

    @Autowired
    private PersonHelper personHelper;

    @Autowired
    private InvolvementHelper involvementHelper;

    @Autowired
    private PermissionGroupHelper permissionHelper;

    @Autowired
    private DataFlowService dataFlowService;

    @Autowired
    private ProposedFlowWorkflowHelper proposedFlowWorkflowHelper;

    @Autowired
    private LogicalFlowHelper lfHelper;

    @Autowired
    private PhysicalSpecHelper psHelper;

    @Autowired
    private PhysicalSpecificationService psSvc;

    @Autowired
    private PhysicalFlowService pfSvc;

    private ProposedFlowCommand baseCreateCommand;
    private EntityReference source;
    private EntityReference target;
    private PhysicalSpecification spec;

    @BeforeEach
    public void setUp() {
        dsl.deleteFrom(org.finos.waltz.schema.tables.ProposedFlow.PROPOSED_FLOW).execute();
        dsl.deleteFrom(PHYSICAL_FLOW).execute();
        dsl.deleteFrom(PHYSICAL_SPECIFICATION).execute();
        dsl.deleteFrom(LOGICAL_FLOW).execute();
        dsl.deleteFrom(ENTITY_WORKFLOW_STATE).execute();
        dsl.deleteFrom(INVOLVEMENT).execute();
        dsl.deleteFrom(PERSON).execute();

        source = appHelper.createNewApp("source", ouIds.a);
        target = appHelper.createNewApp("target", ouIds.a1);
        Long specId = psHelper.createPhysicalSpec(source, "baseSpec");
        spec = psSvc.getById(specId);

        baseCreateCommand = ImmutableProposedFlowCommand.builder()
                .source(source)
                .target(target)
                .specification(spec)
                .flowAttributes(ImmutableFlowAttributes.builder()
                        .basisOffset(0)
                        .frequency(FrequencyKindValue.of("DAILY"))
                        .criticality(CriticalityValue.of("MEDIUM"))
                        .transport(TransportKindValue.UNKNOWN)
                        .build())
                .proposalType(CREATE)
                .reason(proposedFlowWorkflowHelper.getReason())
                .build();
    }

    @Test
    public void testProposedNewFlow() {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 101))
                .target(mkRef(APPLICATION, 202))
                .logicalFlowId(12345)
                .physicalFlowId(12345)
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        // 2. Act --------------------------------------------------------------
        ProposedFlowCommandResponse response = proposedFlowWorkflowService.proposeNewFlow(USER_NAME, command);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(response);
    }

    @Test
    public void testGetProposedFlowDefinition() {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 101))
                .target(mkRef(APPLICATION, 202))
                .logicalFlowId(12345)
                .physicalFlowId(12345)
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        ProposedFlowCommandResponse response = proposedFlowWorkflowService.proposeNewFlow(USER_NAME, command);

        // 2. Act --------------------------------------------------------------
        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.getProposedFlowResponseById(response.proposedFlowId());

        // 3. Assert -----------------------------------------------------------
        assertNotNull(response);
        assertNotNull(proposedFlowResponse);
        assertNotNull((proposedFlowResponse.workflowState().workflowId()));
        assertTrue(proposedFlowResponse.workflowTransitionList().size() > 0);
    }

    @Test
    void testPresenceOfCreateProposalTypeWhenCreatingNewProposedFlow() {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 1011))
                .target(mkRef(APPLICATION, 2022))
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        // 2. Act --------------------------------------------------------------
        ProposedFlowCommandResponse response = proposedFlowWorkflowService.proposeNewFlow(USER_NAME, command);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(response);
        assertNotNull(response.proposedFlowCommand());
        assertEquals(CREATE.name(), response.proposedFlowCommand().proposalType().name());
    }

    @Test
    void testWhenPhysicalFlowAndProposedFlowDoesNotExist() {
        // 1. Arrange ----------------------------------------------------------
        long logicalId = 1L;

        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 10111))
                .target(mkRef(APPLICATION, 20222))
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(CREATE)
                .logicalFlowId(Optional.of(logicalId))
                .build();

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(command.source())
                .target(command.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        // 2. Act --------------------------------------------------------------
        FlowIdResponse flowIdResponse = proposedFlowWorkflowService.validateProposedFlow(command, USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNull(flowIdResponse);
    }

    /*@Test//TODO need to fix context issue later
    void testWhenLogicalFlowExistsAndPhysicalFlowExists() {
        // 1. Arrange ----------------------------------------------------------
        long logicalId = 1L;
        long physicalId = 1L;

        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 101))
                .target(mkRef(APPLICATION, 202))
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(CREATE)
                .logicalFlowId(Optional.of(logicalId))
                .physicalFlowId(Optional.of(physicalId))
                .build();

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(command.source())
                .target(command.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        ImmutablePhysicalFlow.Builder flowBuilder = ImmutablePhysicalFlow.builder()
                .specificationId(1L)
                .name(command.flowAttributes().name())
                .basisOffset(command.flowAttributes().basisOffset())
                .frequency(command.flowAttributes().frequency())
                .transport(command.flowAttributes().transport())
                .criticality(CriticalityValue.of("low"))
                .description(mkSafe(command.flowAttributes().description()))
                .logicalFlowId(1l)
                .lastUpdatedBy(USER_NAME)
                .lastUpdatedAt(now)
                .created(UserTimestamp.mkForUser(USER_NAME, now));

        command
                .flowAttributes()
                .externalId()
                .ifPresent(flowBuilder::externalId);

        PhysicalFlow flow = flowBuilder.build();

        physicalFlowDao.create(flow);

        // 2. Act --------------------------------------------------------------
        FlowIdResponse flowIdResponse =  proposedFlowWorkflowService.validateProposedFlow(command, USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(flowIdResponse);
        assertEquals(physicalId, flowIdResponse.id());
    }
*/
    //@Test//TODO: this test case is passing in local but failing in github finos, need to fix this issue later
    void twoStepApproval_shouldTransitionToFullyApproved_andCallOperation() throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        // Create a proposed flow command for a new flow (CREATE proposal type)
        ProposedFlowCommand createCommand = ImmutableProposedFlowCommand.builder()
                .source(appHelper.createNewApp(mkName(USER_NAME, "appA"), ouIds.a))
                .target(appHelper.createNewApp(mkName(USER_NAME, "appB"), ouIds.a))
                // For a CREATE proposal, logicalFlowId and physicalFlowId should not be set initially
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.CREATE)
                .build();

        String userName = mkName(USER_NAME, "user1");

        // Propose the new flow
        ProposedFlowCommandResponse proposeResponse = proposedFlowWorkflowService.proposeNewFlow(userName, createCommand);
        Long proposedFlowId = proposeResponse.proposedFlowId();
        assertNotNull(proposedFlowId, "Proposed flow should be created");

        // Grant the user source and target approver permissions
        Long personA = personHelper.createPerson(userName);

        long involvementKind = involvementHelper.mkInvolvementKind("rel_abc");
        involvementHelper.createInvolvement(personA, involvementKind, createCommand.source());
        involvementHelper.createInvolvement(personA, involvementKind, createCommand.target());

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, USER_NAME);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, USER_NAME, Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, USER_NAME, REJECT);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.target(), ig, USER_NAME, Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.target(), ig, USER_NAME, REJECT);

        // Action command for source approval
        ProposedFlowActionCommand sourceApproveCommand = ImmutableProposedFlowActionCommand.builder()
                .comment("Approved by source approver")
                .build();

        // 2. Act --------------------------------------------------------------
        // Assert that the flow is now in SOURCE_APPROVED state
        // Simulate a source approver approving the flow
        ProposedFlowResponse sourceApprovedFlow = proposedFlowWorkflowService.proposedFlowAction(
                proposedFlowId,
                APPROVE,
                userName,
                sourceApproveCommand);

        // 2.1. Assert -----------------------------------------------------------
        // Assert that the flow is now in SOURCE_APPROVED state
        assertEquals(SOURCE_APPROVED.name(), sourceApprovedFlow.workflowState().state(), "Flow should be in SOURCE_APPROVED state");

        // Action command for target approval
        ProposedFlowActionCommand targetApproveCommand = ImmutableProposedFlowActionCommand.builder()
                .comment("Approved by target approver")
                .build();

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefinition.id().get(), mkRef(PROPOSED_FLOW, proposedFlowId), USER_NAME, FULLY_APPROVED.name(), "test");

        //Create new logical flow, physical specification and physical flow
        EntityReference a = createCommand.source();
        EntityReference b = createCommand.target();

        LogicalFlow ab = lfHelper.createLogicalFlow(a, b);
        Long specId = psHelper.createPhysicalSpec(a, mkName("create"));
        PhysicalSpecification spec = psSvc.getById(specId);

        ImmutableFlowAttributes flowAttrs = ImmutableFlowAttributes.builder()
                .frequency(FrequencyKindValue.of("DAILY"))
                .criticality(CriticalityValue.of("MEDIUM"))
                .transport(TransportKindValue.UNKNOWN)
                .basisOffset(0)
                .build();

        ImmutablePhysicalFlowCreateCommand physicalFlwCreateCommand = ImmutablePhysicalFlowCreateCommand.builder()
                .logicalFlowId(ab.entityReference().id())
                .specification(spec)
                .flowAttributes(flowAttrs)
                .build();

        PhysicalFlowCreateCommandResponse createResp = pfSvc.create(physicalFlwCreateCommand, mkName("create"));
        assertEquals(CommandOutcome.SUCCESS, createResp.outcome(), "Can successfully create physical flows");


        // 3. Act --------------------------------------------------------------
        // Simulate a target approver approving the flow
        ProposedFlowResponse fullyApprovedFlow = proposedFlowWorkflowService.proposedFlowAction(
                proposedFlowId,
                APPROVE,
                userName,
                targetApproveCommand);

        // 3.1 Assert -----------------------------------------------------------
        assertNotNull(fullyApprovedFlow, "Approved proposed flow should not be null");
        assertEquals(FULLY_APPROVED.name(), fullyApprovedFlow.workflowState().state(), "Flow should be fully approved after source approval");
        assertNotNull(fullyApprovedFlow.logicalFlowId());
        assertNotNull(fullyApprovedFlow.physicalFlowId());
        assertNotNull(fullyApprovedFlow.specificationId());

    }

    @Test
    void proposedFlowAction_rejectionWithInvalidPermissions_throwsException() throws FlowCreationException, TransitionNotFoundException {
        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        ProposedFlowCommand createCommand = ImmutableProposedFlowCommand.builder()
                .source(appHelper.createNewApp(mkName(USER_NAME, "appA_1"), ouIds.a))
                .target(appHelper.createNewApp(mkName(USER_NAME, "appB_1"), ouIds.a))
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.CREATE)
                .build();

        String userName = mkName(USER_NAME, "user1");
        personHelper.createPerson(userName);

        ProposedFlowCommandResponse proposeResponse = proposedFlowWorkflowService.proposeNewFlow(userName, createCommand);
        Long proposedFlowId = proposeResponse.proposedFlowId();
        assertNotNull(proposedFlowId, "Proposed flow should be created");

        ProposedFlowActionCommand rejectCommand = ImmutableProposedFlowActionCommand.builder()
                .comment("Rejected due to invalid permissions")
                .build();

        // 2. Act ----------------------------------------------------
        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.proposedFlowAction(
                proposedFlowId,
                org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.REJECT,
                userName,
                rejectCommand);

        // 3. Assert ---------------------------------------------------
        assertNotNull(proposedFlowResponse);
        assertEquals("FAILURE", proposedFlowResponse.outcome().name());
        assertEquals("REJECT Failed. The workflow may have been updated or you no longer have permissions to reject this item.", proposedFlowResponse.message());
    }

    @Test
    public void testCannotApproveProposedFlowTwice() throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {
        // 1. Arrange ----------------------------------------------------------
        Reason reason = proposedFlowWorkflowHelper.getReason();
        EntityReference owningEntity = proposedFlowWorkflowHelper.getOwningEntity();
        PhysicalSpecification physicalSpecification = proposedFlowWorkflowHelper.getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = proposedFlowWorkflowHelper.getFlowAttributes();
        Set<Long> dataTypeIdSet = proposedFlowWorkflowHelper.getDataTypeIdSet();

        // Create a proposed flow command for a new flow (CREATE proposal type)
        ProposedFlowCommand createCommand = ImmutableProposedFlowCommand.builder()
                .source(appHelper.createNewApp(mkName(USER_NAME, "appA"), ouIds.a))
                .target(appHelper.createNewApp(mkName(USER_NAME, "appB"), ouIds.a))
                // For a CREATE proposal, logicalFlowId and physicalFlowId should not be set initially
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.CREATE)
                .build();

        String userName = mkName(USER_NAME, "user1");

        // Propose the new flow
        ProposedFlowCommandResponse proposeResponse = proposedFlowWorkflowService.proposeNewFlow(userName, createCommand);
        Long proposedFlowId = proposeResponse.proposedFlowId();
        assertNotNull(proposedFlowId, "Proposed flow should be created");

        // Grant the user source and target approver permissions
        Long personA = personHelper.createPerson(userName);

        long involvementKind = involvementHelper.mkInvolvementKind("rel_abcd");
        involvementHelper.createInvolvement(personA, involvementKind, createCommand.source());

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, USER_NAME);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, USER_NAME, Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, USER_NAME, REJECT);

        // Action command for source approval
        ProposedFlowActionCommand sourceApproveCommand = ImmutableProposedFlowActionCommand.builder()
                .comment("Approved by source approver")
                .build();

        // 2. Act --------------------------------------------------------------
        // Assert that the flow is now in SOURCE_APPROVED state
        // Simulate a source approver approving the flow
        ProposedFlowResponse sourceApprovedFlow = proposedFlowWorkflowService.proposedFlowAction(
                proposedFlowId,
                APPROVE,
                userName,
                sourceApproveCommand);

        // 2.1. Assert -----------------------------------------------------------
        // Assert that the flow is now in SOURCE_APPROVED state
        assertEquals(SOURCE_APPROVED.name(), sourceApprovedFlow.workflowState().state(), "Flow should be in SOURCE_APPROVED state");

        //Source approver again approving the flow should fail
        // 2. Act ---------------------------------------------------
        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.proposedFlowAction(proposedFlowId, APPROVE, userName, sourceApproveCommand);

        // 3. Assert ---------------------------------------------------
        assertNotNull(proposedFlowResponse);
        assertEquals("FAILURE", proposedFlowResponse.outcome().name());
        assertEquals("APPROVE Failed. The workflow may have been updated or you no longer have permissions to approve this item.", proposedFlowResponse.message());
    }

    @Test
    public void validateProposedFlow_shouldFailIfIdenticalProposalExists() {
        // 1. Arrange: Create and save an initial proposal
        ProposedFlowCommandResponse initialResponse = proposedFlowWorkflowService.proposeNewFlow(USER_NAME, baseCreateCommand);
        assertEquals(CommandOutcome.SUCCESS, initialResponse.outcome(), "Initial proposal should be saved successfully");

        // 2. Act: Validate a second, identical command
        FlowIdResponse validationResponse = proposedFlowWorkflowService.validateProposedFlow(baseCreateCommand, USER_NAME);

        // 3. Assert: Validation should fail and return the ID of the existing proposal
        assertNotNull(validationResponse, "Validation should fail for an identical proposal");
        assertEquals(initialResponse.proposedFlowId(), validationResponse.id(), "Response should contain the ID of the existing proposal");
    }

    @Test
    public void validateProposedFlow_shouldPassIfSpecificationIsDifferent() {
        // 1. Arrange: Create and save an initial proposal
        proposedFlowWorkflowService.proposeNewFlow(USER_NAME, baseCreateCommand);

        // Create a new command with a different specification
        PhysicalSpecification differentSpec = psSvc.getById(psHelper.createPhysicalSpec(source, "differentSpec"));
        ProposedFlowCommand commandWithDifferentSpec = ImmutableProposedFlowCommand
                .copyOf(baseCreateCommand)
                .withSpecification(differentSpec);

        // 2. Act: Validate the new command
        FlowIdResponse validationResponse = proposedFlowWorkflowService.validateProposedFlow(commandWithDifferentSpec, USER_NAME);

        // 3. Assert: Validation should pass
        assertNull(validationResponse, "Validation should pass when specification is different");
    }

    @Test
    public void validateProposedFlow_shouldPassIfFlowAttributeIsDifferent() {
        // 1. Arrange: Create and save an initial proposal
        proposedFlowWorkflowService.proposeNewFlow(USER_NAME, baseCreateCommand);

        // Create a new command with a different frequency
        FlowAttributes differentAttributes = ImmutableFlowAttributes
                .copyOf(baseCreateCommand.flowAttributes())
                .withFrequency(FrequencyKindValue.of("WEEKLY"));

        ProposedFlowCommand commandWithDifferentFrequency = ImmutableProposedFlowCommand
                .copyOf(baseCreateCommand)
                .withFlowAttributes(differentAttributes);

        // 2. Act: Validate the new command
        FlowIdResponse validationResponse = proposedFlowWorkflowService.validateProposedFlow(commandWithDifferentFrequency, USER_NAME);

        // 3. Assert: Validation should pass
        assertNull(validationResponse, "Validation should pass when a flow attribute (e.g., frequency) is different");
    }

    @Test
    public void validateNoConcurrentActionsOnProposedFlows() {
        /*
            Step 1. Create a flow, with the user as source and target approver
            Step 2. Parallelly execute approval actions
            Step 3. One Action succeeds, the other fails
         */

        String testStem = mkName("validateNoConcurrentActionsOnProposedFlows");
        ProposedFlowCommandResponse proposedFlowCommandResponse = proposedFlowWorkflowService.proposeNewFlow(testStem, baseCreateCommand);


        Long personA = personHelper.createPerson(testStem);

        long involvementKind = involvementHelper.mkInvolvementKind("_rel");
        involvementHelper.createInvolvement(personA, involvementKind, baseCreateCommand.source());
        involvementHelper.createInvolvement(personA, involvementKind, baseCreateCommand.target());

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, "_rel_ig");

        permissionHelper.setupPermissionGroupForProposedFlow(baseCreateCommand.source(), ig, "_rel_pg", Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(baseCreateCommand.target(), ig, "_rel_pg", Operation.APPROVE);

        List<ProposedFlowResponse> failures = new ArrayList<>();
        List<ProposedFlowResponse> responses = new ArrayList<>();

        // Run actions in parallel
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(2);

        Runnable approveTaskA = () -> mkRunnable(proposedFlowCommandResponse.proposedFlowId(), testStem, responses, failures, start, end);
        Runnable approveTaskB = () -> mkRunnable(proposedFlowCommandResponse.proposedFlowId(), testStem, responses, failures, start, end);
        new Thread(approveTaskA, "task_A").start();
        new Thread(approveTaskB, "task_B").start();

        try {
            start.countDown();
            assertTrue(end.await(10, TimeUnit.SECONDS), "Concurrent approve tasks did not finish in time");
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }

        assertEquals(1, responses.size());
        assertEquals(1, failures.size());

        // further non-concurrent action should lead to a fully approved state
        ProposedFlowResponse fullyApprovedActionResponse = proposedFlowWorkflowService.proposedFlowAction(proposedFlowCommandResponse.proposedFlowId(),
                APPROVE,
                testStem,
                ImmutableProposedFlowActionCommand
                    .builder()
                    .comment(format("Approved by %s", testStem))
                    .build());

        assertEquals(FULLY_APPROVED.name(), fullyApprovedActionResponse.workflowState().state());
    }

    private void mkRunnable(Long flowId,
                                String user,
                                List<ProposedFlowResponse> responses,
                                List<ProposedFlowResponse> failures,
                                CountDownLatch start,
                                CountDownLatch end) {
        try {
            start.await(5, TimeUnit.SECONDS);
            ProposedFlowResponse proposedFlowActionResponse = proposedFlowWorkflowService.proposedFlowAction(flowId,
                    APPROVE,
                    user,
                    ImmutableProposedFlowActionCommand
                            .builder()
                            .comment(format("Approved by %s", user))
                            .build());

            if(proposedFlowActionResponse.outcome() == CommandOutcome.SUCCESS) {
                synchronized (responses) {
                    responses.add(proposedFlowActionResponse);
                }
            } else {
                synchronized (failures) {
                    failures.add(proposedFlowActionResponse);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted", e);
        } finally {
            end.countDown();
        }
    }
}
