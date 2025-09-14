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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
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
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ImmutableReason;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposalType;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.model.proposed_flow.Reason;
import org.finos.waltz.service.maker_checker.MakerCheckerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.proposed_flow.ProposalType.CREATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MakerCheckerServiceTest extends BaseInMemoryIntegrationTest {

    @Autowired
    MakerCheckerService makerCheckerService;

    @Autowired
    ProposedFlowDao proposedFlowDao;

    @Autowired
    LogicalFlowDao logicalFlowDao;

    @Autowired
    PhysicalSpecificationDao physicalSpecificationDao;

    @Autowired
    EntityWorkflowStateDao entityWorkflowStateDao;

    @Autowired
    EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;

    private static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";

    private static final String USER_NAME = "testUser";

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
        ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(USER_NAME, command);

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

        ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(USER_NAME, command);

        // 2. Act --------------------------------------------------------------
        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(response.proposedFlowId());

        // 3. Assert -----------------------------------------------------------
        assertNotNull(response);
        assertNotNull(proposedFlowResponse);
        assertNotNull((proposedFlowResponse.workflowState().workflowId()));
        assertTrue(proposedFlowResponse.workflowTransitionList().size() > 0);
    }

    @Test
    void testWhenLogicalFlowIdIsNotPresentThenCreatesBothLogicalAndPhysicalFlow() throws JsonProcessingException, FlowCreationException {

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

        Long proposedFlowId = proposedFlowDao.saveProposedFlow(USER_NAME, command);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowId, USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertNotNull(resp.logicalFlow().id());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }

    @Test
    void testWhenLogicalFlowAlreadyExistsThenOnlyPhysicalFlowIsCreated() throws JsonProcessingException, FlowCreationException {

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
                .logicalFlowId(1)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        Long proposedFlowId = proposedFlowDao.saveProposedFlow(USER_NAME, command);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }

    private AddLogicalFlowCommand mapProposedFlowToAddLogicalFlowCommand(ProposedFlowResponse proposedFlow) {
        return ImmutableAddLogicalFlowCommand.builder()
                .source(proposedFlow.flowDef().source())
                .target(proposedFlow.flowDef().target())
                .build();
    }

    @Test
    void testPhysicalSpecificationCreationWhenPhysicalFlowIsCreated() throws FlowCreationException, JsonProcessingException {

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
                .logicalFlowId(1)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        Long proposedFlowId = proposedFlowDao.saveProposedFlow(USER_NAME, command);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        PhysicalSpecification specification = ImmutablePhysicalSpecification
                .copyOf(proposedFlowResponse.flowDef().specification())
                .withLastUpdatedBy(USER_NAME)
                .withLastUpdatedAt(now)
                .withCreated(UserTimestamp.mkForUser(USER_NAME, now));

        long specId = physicalSpecificationDao.create(specification);
        PhysicalSpecification physicalSpec = physicalSpecificationDao.getById(specId);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);

        assertNotNull(physicalSpec);
        assertTrue(physicalSpec.id().get() > 0L);
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
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .build();
    }

    private PhysicalSpecification getExistingPhysicalSpecification(EntityReference owningEntity) {
        return ImmutablePhysicalSpecification.builder()
                .id(112318)
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
    void testPresenceOfCreateProposalTypeWhenCreatingNewProposedFlow(){

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
        ProposedFlowCommandResponse response = makerCheckerService.proposeNewFlow(USER_NAME, command);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(response);
        assertNotNull(response.proposedFlowCommand());
        assertEquals(CREATE.name(), response.proposedFlowCommand().proposalType().name());
    }

    @Test
    void testCheckPresenceOfSpecificationIdInResponseWhenNewSpecificationIsSelected() throws JsonProcessingException, FlowCreationException {

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
                .logicalFlowId(1)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        Long proposedFlowId = proposedFlowDao.saveProposedFlow(USER_NAME, command);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        PhysicalSpecification specification = ImmutablePhysicalSpecification
                .copyOf(proposedFlowResponse.flowDef().specification())
                .withLastUpdatedBy(USER_NAME)
                .withLastUpdatedAt(now)
                .withCreated(UserTimestamp.mkForUser(USER_NAME, now));

        long specId = physicalSpecificationDao.create(specification);
        physicalSpecificationDao.getById(specId);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().specificationId() > 0);
    }

    @Test
    void testCheckPresenceOfSpecificationIdInResponseWhenExistingSpecificationsIsSelected() throws JsonProcessingException, FlowCreationException {

        // 1. Arrange ----------------------------------------------------------
        Reason reason = getReason();
        EntityReference owningEntity = getOwningEntity();
        PhysicalSpecification physicalSpecification = getExistingPhysicalSpecification(owningEntity);
        FlowAttributes flowAttributes = getFlowAttributes();
        Set<Long> dataTypeIdSet = getDataTypeIdSet();

        ProposedFlowCommand command = ImmutableProposedFlowCommand.builder()
                .source(mkRef(APPLICATION, 101))
                .target(mkRef(APPLICATION, 202))
                .reason(reason)
                .specification(physicalSpecification)
                .flowAttributes(flowAttributes)
                .dataTypeIds(dataTypeIdSet)
                .logicalFlowId(1)
                .proposalType(ProposalType.valueOf("CREATE"))
                .build();

        Long proposedFlowId = proposedFlowDao.saveProposedFlow(USER_NAME, command);

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = ProposedFlowWorkflowState.FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = makerCheckerService.getProposedFlowById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);

        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        logicalFlowDao.addFlow(flowToAdd);

        PhysicalSpecification specification = ImmutablePhysicalSpecification
                .copyOf(proposedFlowResponse.flowDef().specification())
                .withLastUpdatedBy(USER_NAME)
                .withLastUpdatedAt(now)
                .withCreated(UserTimestamp.mkForUser(USER_NAME, now));

        long specId = physicalSpecificationDao.create(specification);
        physicalSpecificationDao.getById(specId);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = makerCheckerService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().specificationId() > 0);
        assertEquals(112318, resp.physicalFlowCreateCommandResponse().specificationId());
    }
}