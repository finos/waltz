package org.finos.waltz.integration_test.inmem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.physical_flow.PhysicalFlowDao;
import org.finos.waltz.data.physical_specification.PhysicalSpecificationDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.integration_test.inmem.BaseInMemoryIntegrationTest;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.*;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.data_flow.DataFlowService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.proposed_flow_workflow.ProposedFlowWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.data.proposed_flow.ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW;
import static org.finos.waltz.model.EntityKind.APPLICATION;
import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.EntityLifecycleStatus.ACTIVE;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.FULLY_APPROVED;
import static org.junit.jupiter.api.Assertions.*;

public class DataFlowServiceTest extends BaseInMemoryIntegrationTest {
    private static final String USER_NAME = "testUser";

    @Autowired
    LogicalFlowDao logicalFlowDao;

    @Autowired
    DataFlowService dataFlowService;

    @Autowired
    EntityWorkflowService entityWorkflowService;

    @Autowired
    PhysicalSpecificationDao physicalSpecificationDao;

    @Autowired
    EntityWorkflowStateDao entityWorkflowStateDao;

    @Autowired
    ProposedFlowDao proposedFlowDao;

    @Autowired
    ProposedFlowWorkflowService proposedFlowWorkflowService;

    @Autowired
    ChangeLogService changeLogService;

    @Autowired
    PhysicalFlowDao physicalFlowDao;

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

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, entityReference, USER_NAME,
                workflowState.name(), description);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowId, USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertNotNull(resp.logicalFlow().id());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
    }


    //    @Test
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

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.getProposedFlowResponseById(proposedFlowId);
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
        LogicalPhysicalFlowCreationResponse resp = dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

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

    //    @Test
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

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.getProposedFlowResponseById(proposedFlowId);
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
        LogicalPhysicalFlowCreationResponse resp = dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);

        assertNotNull(physicalSpec);
        assertTrue(physicalSpec.id().get() > 0L);
    }

    //    @Test
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

        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = FULLY_APPROVED;
        String description = "test description";

        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);

        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.getProposedFlowResponseById(proposedFlowId);
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
        LogicalPhysicalFlowCreationResponse resp = dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());

        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().specificationId() > 0);
    }

    //    @Test
    void testCheckPresenceOfSpecificationIdLogicalAndPhysicalFlowIdInResponseWhenExistingSpecificationsIsSelected() throws JsonProcessingException, FlowCreationException {

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
        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        Long entityWorkflowDefId = entityWorkflowDefinition.id().get();
        EntityReference entityReference = mkRef(PROPOSED_FLOW, proposedFlowId);
        ProposedFlowWorkflowState workflowState = FULLY_APPROVED;
        String description = "test description";
        entityWorkflowStateDao.createWorkflowState(entityWorkflowDefId, EntityReference.mkRef(entityReference.kind(), proposedFlowId), USER_NAME,
                workflowState.name(), description);
        ProposedFlowResponse proposedFlowResponse = proposedFlowWorkflowService.getProposedFlowResponseById(proposedFlowId);
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlowResponse);
        LocalDateTime now = nowUtc();
        LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                .source(addCmd.source())
                .target(addCmd.target())
                .lastUpdatedAt(now)
                .lastUpdatedBy(USER_NAME)
                .created(UserTimestamp.mkForUser(USER_NAME, now))
                .build();

        LogicalFlow logicalFlow = logicalFlowDao.addFlow(flowToAdd);

        ImmutablePhysicalFlow.Builder flowBuilder = ImmutablePhysicalFlow.builder()
                .specificationId(command.specification().id().get())
                .name(command.flowAttributes().name())
                .basisOffset(command.flowAttributes().basisOffset())
                .frequency(command.flowAttributes().frequency())
                .transport(command.flowAttributes().transport())
                .criticality(command.flowAttributes().criticality())
                .description(mkSafe(command.flowAttributes().description()))
                .logicalFlowId(logicalFlow.id().get())
                .lastUpdatedBy(USER_NAME)
                .lastUpdatedAt(now)
                .created(UserTimestamp.mkForUser(USER_NAME, now));

        command
                .flowAttributes()
                .externalId()
                .ifPresent(flowBuilder::externalId);

        PhysicalFlow flow = flowBuilder.build();

        long physicalFlowId = physicalFlowDao.create(flow);

        physicalSpecificationDao.create(createPhysicalSpecification(owningEntity));

        changeLogService.writeChangeLogEntries(
                ImmutablePhysicalFlow.copyOf(flow).withId(physicalFlowId),
                USER_NAME,
                " created",
                Operation.ADD);

        // 2. Act --------------------------------------------------------------
        LogicalPhysicalFlowCreationResponse resp = dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowResponse.id(), USER_NAME);

        // 3. Assert -----------------------------------------------------------
        assertNotNull(resp.logicalFlow());
        assertEquals(resp.logicalFlow().id().get(), command.logicalFlowId().get());
        assertNotNull(resp.physicalFlowCreateCommandResponse());
        assertTrue(resp.physicalFlowCreateCommandResponse().entityReference().id() > 0);
        assertTrue(resp.physicalFlowCreateCommandResponse().specificationId() > 0);
        assertEquals(command.specification().id().get(), resp.physicalFlowCreateCommandResponse().specificationId());
    }

    private PhysicalSpecification getExistingPhysicalSpecification(EntityReference owningEntity) {
        return ImmutablePhysicalSpecification.builder()
                .id(1)
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .build();
    }

    private PhysicalSpecification createPhysicalSpecification(EntityReference owningEntity) {
        return ImmutablePhysicalSpecification.builder()
                .owningEntity(owningEntity)
                .name("mc_specification")
                .description("mc_specification description")
                .format(DataFormatKindValue.of("DATABASE"))
                .lastUpdatedBy("waltz")
                .externalId("mc-extId001")
                .created(UserTimestamp.mkForUser(USER_NAME, now()))
                .build();
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
}
