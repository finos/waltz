package org.finos.waltz.service.maker_checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.changelog.ChangeLogDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.proposed_flow.ImmutableLogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.LogicalPhysicalFlowCreationResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.workflow_state_machine.WorkflowDefinition;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowContext;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction;
import org.jooq.DSLContext;
import org.jooq.exception.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;


@Service
public class MakerCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);

    private static final String PROPOSED_FLOW_CREATED_WITH_SUCCESS = "PROPOSED_FLOW_CREATED_WITH_SUCCESS";
    private static final String PROPOSED_FLOW_CREATED_WITH_FAILURE = "PROPOSED_FLOW_CREATED_WITH_FAILURE";
    private static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final ProposedFlowDao proposedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;
    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;
    private final DSLContext dslContext;
    private final ChangeLogDao changeLogDao;
    private final WorkflowDefinition proposedFlowWorkflowDefinition;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowService physicalFlowService;
    private final LogicalFlowDao logicalFlowDao;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        EntityWorkflowDefinitionDao entityWorkflowDefinitionDao,
                        ProposedFlowDao proposedFlowDao,
                        DSLContext dslContext,
                        ChangeLogDao changeLogDao,
                        WorkflowDefinition proposedFlowWorkflowDefinition,
                        LogicalFlowService logicalFlowService,
                        PhysicalFlowService physicalFlowService,
                        LogicalFlowDao logicalFlowDao) {
        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(entityWorkflowStateDao, "entityWorkflowStateDao cannot be null");
        checkNotNull(entityWorkflowTransitionDao, "entityWorkflowTransitionDao cannot be null");
        checkNotNull(entityWorkflowDefinitionDao, "entityWorkflowDefinitionDao cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(changeLogDao, "changeLogDao cannot be null");
        checkNotNull(proposedFlowWorkflowDefinition, "proposedFlowWorkflowDefinition cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");

        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.entityWorkflowDefinitionDao = entityWorkflowDefinitionDao;
        this.proposedFlowDao = proposedFlowDao;
        this.dslContext = dslContext;
        this.changeLogDao = changeLogDao;
        this.proposedFlowWorkflowDefinition = proposedFlowWorkflowDefinition;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowService = physicalFlowService;
        this.logicalFlowDao = logicalFlowDao;
    }

    public ProposedFlowCommandResponse proposeNewFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand) {

        AtomicReference<Long> proposedFlowId = new AtomicReference<>(-1L);
        AtomicReference<EntityWorkflowDefinition> entityWorkflowDefinition = new AtomicReference<>();
        String msg = PROPOSED_FLOW_CREATED_WITH_SUCCESS;
        String outcome = CommandOutcome.SUCCESS.name();
        try {
            dslContext.transaction(dslContext -> {
                DSLContext dsl = dslContext.dsl();
                proposedFlowId.set(proposedFlowDao.saveProposedFlow(requestBody, username, proposedFlowCommand));
                LOG.info("New ProposedFlowId is : {} ", proposedFlowId);
                entityWorkflowDefinition.set(entityWorkflowService.searchByName("Propose Flow Lifecycle Workflow"));
                if (entityWorkflowDefinition.get().id().isPresent()) {
                    // 1. Get the state machine from the definition
                    WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext>
                            proposedFlowStateMachine = proposedFlowWorkflowDefinition.getMachine();

                    // 2. Get current state and build context
                    ProposedFlowWorkflowContext workflowContext = new ProposedFlowWorkflowContext(
                            entityWorkflowDefinition.get().id().get(),
                            proposedFlowId.get(), EntityKind.PROPOSED_FLOW.name(), username, "reason");

                    // 3. Fire the action
                    ProposedFlowWorkflowState newState = proposedFlowStateMachine.fire(PROPOSED_CREATE, PROPOSE, workflowContext);
                    entityWorkflowStateDao.createWorkflowState(proposedFlowId.get(), entityWorkflowDefinition.get().id().get(),
                            username, EntityKind.PROPOSED_FLOW, newState, "Proposed Flow Submitted");
                    entityWorkflowTransitionDao.createWorkflowTransition(proposedFlowId.get(), entityWorkflowDefinition.get().id().get(),
                            username, EntityKind.PROPOSED_FLOW, PROPOSED_CREATE, newState, "flow proposed");
                } else {
                    throw new NoDataFoundException("Could not find workflow definition: Propose Flow Lifecycle Workflow");
                }

                List<ChangeLog> changeLogList = new ArrayList<>();
                changeLogList.add(createChangeLogObject("New Proposed Flow Created", username, proposedFlowId.get()));
                changeLogList.add(createChangeLogObject("Entity Workflow State changed to Submitted", username, proposedFlowId.get()));
                changeLogList.add(createChangeLogObject("Entity Workflow Transition saved with from: Proposed-Create to: Action-Pending State", username, proposedFlowId.get()));
                changeLogDao.write(changeLogList);

            });
        } catch (Exception e) {
            msg = PROPOSED_FLOW_CREATED_WITH_FAILURE;
            outcome = CommandOutcome.FAILURE.name();
            LOG.info("Error Occurred : {} ", e.getMessage());
            e.printStackTrace();
        }
        return ImmutableProposedFlowCommandResponse.builder()
                .message(msg)
                .outcome(outcome)
                .proposedFlowCommand(proposedFlowCommand)
                .proposedFlowId(proposedFlowId.get())
                .workflowDefinitionId(entityWorkflowDefinition.get().id().isPresent() ? entityWorkflowDefinition.get().id().get() : -1L)
                .build();
    }

    public ChangeLog createChangeLogObject(String msg, String userName, Long proposedFlowId) {
        return ImmutableChangeLog
                .builder()
                .message(msg)
                .userId(userName)
                .parentReference(mkRef(EntityKind.PROPOSED_FLOW, proposedFlowId))
                .operation(Operation.ADD)
                .severity(Severity.INFORMATION)
                .build();
    }

    /**
     * Retrieves a proposed flow by its primary key.
     *
     * @param id the flow's primary key
     * @return ProposedFlowResponse
     */
    public ProposedFlowResponse getProposedFlowById(long id) {
        ProposedFlowRecord proposedFlowRecord = proposedFlowDao.getProposedFlowById(id);
        if (proposedFlowRecord == null) {
            throw new NoSuchElementException("ProposedFlow not found: " + id);
        }
        EntityReference entityReference = mkRef(EntityKind.PROPOSED_FLOW, id);
        EntityWorkflowDefinition entityWorkflowDefinition = entityWorkflowDefinitionDao.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        Long workFlowId = Optional.ofNullable(entityWorkflowDefinition)
                .flatMap(EntityWorkflowDefinition::id)
                .orElseThrow(() -> new NoSuchElementException("Propose Flow Lifecycle Workflow not found"));
        EntityWorkflowState entityWorkflowState = entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(workFlowId, entityReference);
        List<EntityWorkflowTransition> entityWorkflowTransitionList = entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workFlowId, entityReference);
        try {

            ProposedFlowCommand flowDefinition = getJsonMapper().readValue(proposedFlowRecord.getFlowDef(), ProposedFlowCommand.class);

            return ImmutableProposedFlowResponse.builder()
                    .id(proposedFlowRecord.getId())
                    .sourceEntityId(proposedFlowRecord.getSourceEntityId())
                    .sourceEntityKind(proposedFlowRecord.getSourceEntityKind())
                    .targetEntityId(proposedFlowRecord.getTargetEntityId())
                    .targetEntityKind(proposedFlowRecord.getTargetEntityKind())
                    .createdAt(proposedFlowRecord.getCreatedAt().toLocalDateTime())
                    .createdBy(proposedFlowRecord.getCreatedBy())
                    .flowDef(flowDefinition)
                    .workflowState(entityWorkflowState)
                    .workflowTransitionList(entityWorkflowTransitionList)
                    .build();
        } catch (JsonProcessingException e) {
            LOG.error("Invalid flow definition JSON : {} ", e.getMessage());
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }
    }

    /**
     * Creates logical and physical flows from the ProposedFlow.
     *
     * @param proposedFlowId primary key of the ProposedFlow
     * @param username       actor requesting the creation
     * @return immutable response containing the created flows
     * @throws FlowCreationException if either creation step fails
     */
    public LogicalPhysicalFlowCreationResponse createLogicalAndPhysicalFlowFromProposedFlowDef(long proposedFlowId, String username) throws FlowCreationException {

        requireNonNull(username, "username must not be null");
        PhysicalFlowCreateCommandResponse physicalFlow;
        LogicalFlow logicalFlow;

        ProposedFlowResponse proposedFlow = getProposedFlowById(proposedFlowId);
        LOG.info("Proposed flow definition : {}", proposedFlow);

        Optional<Long> logicalFlowId = proposedFlow.flowDef().logicalFlowId();

        if (logicalFlowId.isPresent()) {
            LOG.debug("Logical flow already exists, logical flow id is : {}, so skipping creation", logicalFlowId.get());
            logicalFlow = logicalFlowDao.getByFlowId(logicalFlowId.get());
        } else {
            //create logical flow
            logicalFlow = createLogicalFlow(proposedFlow, username);
        }

        //create physical flow
        physicalFlow = createPhysicalFlow(proposedFlow, username);

        LOG.info("Successfully created flows for proposedFlowId = {}", proposedFlowId);

        return ImmutableLogicalPhysicalFlowCreationResponse.builder()
                .logicalFlow(logicalFlow)
                .physicalFlowCreateCommandResponse(physicalFlow)
                .build();
    }

    private AddLogicalFlowCommand mapProposedFlowToAddLogicalFlowCommand(ProposedFlowResponse proposedFlow) {
        return ImmutableAddLogicalFlowCommand.builder()
                .source(proposedFlow.flowDef().source())
                .target(proposedFlow.flowDef().target())
                .build();
    }

    private PhysicalFlowCreateCommand mapProposedFlowToPhysicalFlowCreateCommand(ProposedFlowResponse proposedFlow) {
        Optional<Long> logicalFlowId = proposedFlow.flowDef().logicalFlowId();

        return ImmutablePhysicalFlowCreateCommand.builder()
                .specification(proposedFlow.flowDef().specification())
                .logicalFlowId(logicalFlowId.orElse(0L))
                .flowAttributes(proposedFlow.flowDef().flowAttributes())
                .dataTypeIds(proposedFlow.flowDef().dataTypeIds())
                .build();
    }

    private LogicalFlow createLogicalFlow(ProposedFlowResponse proposedFlow, String username) throws FlowCreationException {
        AddLogicalFlowCommand addCmd = mapProposedFlowToAddLogicalFlowCommand(proposedFlow);

        LOG.info("User: {}, adding new logical flow: {}", username, addCmd);
        try {
            return logicalFlowService.addFlow(addCmd, username);
        } catch (Exception ex) {
            LOG.error("Failed to create logical flow from proposedFlowId={}", proposedFlow.id(), ex);
            throw new FlowCreationException("Logical flow creation failed", ex);
        }
    }

    private PhysicalFlowCreateCommandResponse createPhysicalFlow(ProposedFlowResponse proposedFlow, String username) throws FlowCreationException {
        PhysicalFlowCreateCommand command = mapProposedFlowToPhysicalFlowCreateCommand(proposedFlow);

        LOG.info("User: {}, adding new physical flow: {}", username, command);
        try {
            return physicalFlowService.create(command, username);
        } catch (Exception ex) {
            LOG.error("Failed to create physical flow from proposedFlowId={}", proposedFlow.id(), ex);
            throw new FlowCreationException("Physical flow creation failed", ex);
        }
    }
}
