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
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.CriticalityValue;
import org.finos.waltz.model.physical_flow.FlowAttributes;
import org.finos.waltz.model.physical_flow.FrequencyKindValue;
import org.finos.waltz.model.physical_flow.ImmutableFlowAttributes;
import org.finos.waltz.model.physical_flow.TransportKindValue;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.FlowIdResponse;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowActionCommand;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ImmutableReason;
import org.finos.waltz.model.proposed_flow.ProposalType;
import org.finos.waltz.model.proposed_flow.ProposedFlowActionCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.model.proposed_flow.Reason;
import org.finos.waltz.schema.tables.records.InvolvementGroupRecord;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_flow.DataFlowService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.finos.waltz.test_common.helpers.AppHelper;
import org.finos.waltz.test_common.helpers.InvolvementHelper;
import org.finos.waltz.test_common.helpers.PermissionGroupHelper;
import org.finos.waltz.test_common.helpers.PersonHelper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.data.proposed_flow.ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW;
import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.Operation.REJECT;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.FULLY_APPROVED;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.proposed_flow.ProposalType.CREATE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.APPROVE;
import static org.finos.waltz.test_common.helpers.NameHelper.mkName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProposedFlowWorkflowServiceTest extends BaseInMemoryIntegrationTest {
    private static final String USER_NAME = "testUser";

    private final String stem = "pgst";

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
    DataFlowService dataFlowService;



    @BeforeEach
    public void setup() {
        dsl.deleteFrom(INVOLVEMENT).execute();
        dsl.deleteFrom(PHYSICAL_FLOW).execute();
        dsl.deleteFrom(PERSON).execute();
        dsl.deleteFrom(INVOLVEMENT_KIND).execute();
    }

    @Test
    public void testProposedNewFlow() {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

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
        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

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

    private Reason getReason() {
        return ImmutableReason.builder()
                .description("test")
                .ratingId(1)
                .build();
    }

    private EntityReference getOwningEntity() {
        return ImmutableEntityReference.builder()
                .id(18703)
                .kind(APPLICATION)
                .name("AMG")
                .externalId("60487-1")
                .description("Testing")
                .entityLifecycleStatus(ACTIVE)
                .build();
    }

    private PhysicalSpecification getPhysicalSpecification(EntityReference owningEntity) {
        return ImmutablePhysicalSpecification.builder()
                .id(1L)
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .build();
    }

    private FlowAttributes getFlowAttributes() {
        return ImmutableFlowAttributes.builder()
                .name("mc_deliverCharacterstics")
                .transport(TransportKindValue.of("UNKNOWN"))
                .frequency(FrequencyKindValue.of("QUARTERLY"))
                .basisOffset(0)
                .criticality(CriticalityValue.of("low"))
                .description("testing")
                .externalId("567s")
                .build();
    }

    private Set<Long> getDataTypeIdSet() {
        Set<Long> dataTypeIdSet = new HashSet<>();
        dataTypeIdSet.add(41200L);

        return dataTypeIdSet;
    }

    @Test
    void testPresenceOfCreateProposalTypeWhenCreatingNewProposedFlow() {

        // 1. Arrange ----------------------------------------------------------
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
        FlowIdResponse flowIdResponse =  proposedFlowWorkflowService.validateProposedFlow(command, USER_NAME);

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
    @Test
    void twoStepApproval_shouldTransitionToFullyApproved_andCallOperation() throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

        // Create a proposed flow command for a new flow (CREATE proposal type)
        ProposedFlowCommand createCommand = ImmutableProposedFlowCommand.builder()
                .source(appHelper.createNewApp(mkName(stem, "appA"), ouIds.a))
                .target(appHelper.createNewApp(mkName(stem, "appB"), ouIds.a))
                // For a CREATE proposal, logicalFlowId and physicalFlowId should not be set initially
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .proposalType(ProposalType.CREATE)
                .build();

        String userName = mkName(stem, "user1");

        // Propose the new flow
        ProposedFlowCommandResponse proposeResponse = proposedFlowWorkflowService.proposeNewFlow(userName, createCommand);
        Long proposedFlowId = proposeResponse.proposedFlowId();
        assertNotNull(proposedFlowId, "Proposed flow should be created");

        // Grant the user source and target approver permissions
        Long personA = personHelper.createPerson(userName);

        long involvementKind = involvementHelper.mkInvolvementKind("rel_abc");
        involvementHelper.createInvolvement(personA, involvementKind, createCommand.source());
        involvementHelper.createInvolvement(personA, involvementKind, createCommand.target());

        InvolvementGroupRecord ig = permissionHelper.setupInvolvementGroup(involvementKind, stem);

        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, stem, Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.source(), ig, stem, REJECT);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.target(), ig, stem, Operation.APPROVE);
        permissionHelper.setupPermissionGroupForProposedFlow(createCommand.target(), ig, stem, REJECT);

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
        assertEquals(ProposedFlowWorkflowState.SOURCE_APPROVED.name(), sourceApprovedFlow.workflowState().state(), "Flow should be in SOURCE_APPROVED state");

        // Action command for target approval
        ProposedFlowActionCommand targetApproveCommand = ImmutableProposedFlowActionCommand.builder()
                .comment("Approved by target approver")
                .build();

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefinition.id().get(), mkRef(PROPOSED_FLOW, proposedFlowId), USER_NAME, FULLY_APPROVED.name(), "test");

        //Create new logical flow
        ProposedFlowResponse proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlowId);
        LogicalFlow logicalFlow = dataFlowService.createLogicalFlow(proposedFlow, userName);

        //Create new physical flow and physical specification
        physicalSpecificationDao.create(createPhysicalSpecification(owningEntity, userName));
        dataFlowService.createPhysicalFlow(proposedFlow, userName, logicalFlow.id());

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

    private PhysicalSpecification createPhysicalSpecification(EntityReference owningEntity, String userName) {
        return ImmutablePhysicalSpecification.builder()
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .created(UserTimestamp.mkForUser(userName, now()))
                .build();
    }

}