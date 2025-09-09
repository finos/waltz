package org.finos.waltz.service.maker_checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowView;
import org.finos.waltz.model.logical_flow.AddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.ImmutableAddLogicalFlowCommand;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.ImmutablePhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommand;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.model.utils.ProposeFlowPermission;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.workflow_state_machine.WorkflowDefinition;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowContext;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.valueOf;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;


@Service
public class MakerCheckerService {
    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);
    private static final String PROPOSED_FLOW_CREATED_WITH_SUCCESS = "PROPOSED_FLOW_CREATED_WITH_SUCCESS";
    private static final String PROPOSED_FLOW_CREATED_WITH_FAILURE = "PROPOSED_FLOW_CREATED_WITH_FAILURE";
    private static final String PROPOSE_FLOW_LIFECYCLE_WORKFLOW = "Propose Flow Lifecycle Workflow";
    private static final EntityKind PROPOSED_FLOW_ENTITY_KIND = EntityKind.PROPOSED_FLOW;

    private final EntityWorkflowService entityWorkflowService;
    private final ProposedFlowDao proposedFlowDao;
    private final ProposedFlowIdSelectorFactory proposedFlowIdSelectorFactory = new ProposedFlowIdSelectorFactory();

    private final WorkflowDefinition proposedFlowWorkflowDefinition;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowService physicalFlowService;
    private final MakerCheckerPermissionService permissionService;
    private final WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext>
            proposedFlowStateMachine;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        ProposedFlowDao proposedFlowDao,
                        DSLContext dslContext,
                        WorkflowDefinition proposedFlowWorkflowDefinition,
                        LogicalFlowService logicalFlowService,
                        PhysicalFlowService physicalFlowService,
                        MakerCheckerPermissionService permissionService) {
        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(proposedFlowWorkflowDefinition, "proposedFlowWorkflowDefinition cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(permissionService, "MakerCheckerPermissionService cannot be null");

        this.entityWorkflowService = entityWorkflowService;
        this.proposedFlowDao = proposedFlowDao;
        this.proposedFlowWorkflowDefinition = proposedFlowWorkflowDefinition;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowService = physicalFlowService;
//        Get the state machine from the definition
        proposedFlowStateMachine = proposedFlowWorkflowDefinition.getMachine();
        this.permissionService = permissionService;
    }

    public ProposedFlowCommandResponse proposeNewFlow(String username, ProposedFlowCommand proposedFlowCommand) {
        String msg = PROPOSED_FLOW_CREATED_WITH_SUCCESS;
        String outcome = CommandOutcome.SUCCESS.name();
        Long proposedFlowId = null;
        EntityWorkflowDefinition workflowDefinition = null;
        try {
            proposedFlowId = proposedFlowDao.saveProposedFlow(username, proposedFlowCommand);
            EntityReference proposedFlowRef = mkRef(PROPOSED_FLOW_ENTITY_KIND, proposedFlowId);
            workflowDefinition = entityWorkflowService.searchByName(PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

            // Get current state and build context
            ProposedFlowWorkflowContext workflowContext = new ProposedFlowWorkflowContext(
                    workflowDefinition.id().get(),
                    proposedFlowRef, username, proposedFlowCommand.reason().description());

            // Fire the action
            ProposedFlowWorkflowState newState = proposedFlowStateMachine.fire(PROPOSED_CREATE, PROPOSE, workflowContext);
            entityWorkflowService.createEntityWorkflow(proposedFlowRef, workflowDefinition.id().get(),
                    username, "Proposed Flow Submitted", PROPOSED_CREATE.name(), newState.name(), proposedFlowCommand.reason().description());
        } catch (Exception e) {
            msg = PROPOSED_FLOW_CREATED_WITH_FAILURE;
            outcome = CommandOutcome.FAILURE.name();
            LOG.error("Error Occurred : {} ", e.getMessage());
        }

        return ImmutableProposedFlowCommandResponse.builder()
                .message(msg)
                .outcome(outcome)
                .proposedFlowCommand(proposedFlowCommand)
                .proposedFlowId(proposedFlowId)
                .workflowDefinitionId(workflowDefinition != null ? workflowDefinition.id().get() : null)
                .build();
    }

    public ProposedFlowResponse getProposedFlowById(long id) {
        ProposedFlowRecord proposedFlowRecord = proposedFlowDao.getProposedFlowById(id);
        return getProposedFlow(proposedFlowRecord);
    }

    private ProposedFlowResponse getProposedFlow(ProposedFlowRecord proposedFlowRecord) {
        checkNotNull(proposedFlowRecord, format("ProposedFlow not found: %d", proposedFlowRecord.getId()));

        EntityReference entityReference = mkRef(PROPOSED_FLOW_ENTITY_KIND, proposedFlowRecord.getId());
        EntityWorkflowView entityWorkflowView = entityWorkflowService.getEntityWorkflowView(PROPOSE_FLOW_LIFECYCLE_WORKFLOW, entityReference);
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
                    .workflowState(entityWorkflowView.workflowState())
                    .workflowTransitionList(entityWorkflowView.workflowTransitionList())
                    .build();

        } catch (JsonProcessingException e) {
            LOG.error("Invalid flow definition JSON : {} ", e.getMessage());
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }
    }

    public List<ProposedFlowResponse> getProposedFlows() {
        List<ProposedFlowRecord> proposedFlowRecords = proposedFlowDao.getProposedFlows();
        return proposedFlowRecords.stream()
                .map(record -> getProposedFlow(record))
                .collect(Collectors.toList());
    }

    public List<ProposedFlowResponse> getProposedFlowsBySelector(IdSelectionOptions options) {

        return proposedFlowDao.getProposedFlowsBySelector(proposedFlowIdSelectorFactory.apply(options)).stream()
                .map(record -> getProposedFlow(record))
                .collect(Collectors.toList());
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
            logicalFlow = logicalFlowService.getById(logicalFlowId.get());
        } else {
            //create logical flow
            logicalFlow = createLogicalFlow(proposedFlow, username);
        }

        //create physical flow
        physicalFlow = createPhysicalFlow(proposedFlow, username, logicalFlow.id());

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

    private PhysicalFlowCreateCommand mapProposedFlowToPhysicalFlowCreateCommand(ProposedFlowResponse proposedFlow, Optional<Long> logicalFlowId) {
        return ImmutablePhysicalFlowCreateCommand.builder()
                .specification(proposedFlow.flowDef().specification())
                .logicalFlowId(proposedFlow.flowDef().logicalFlowId().orElse(logicalFlowId.get()))
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

    private PhysicalFlowCreateCommandResponse createPhysicalFlow(ProposedFlowResponse proposedFlow, String username, Optional<Long> logicalFlowId) throws FlowCreationException {
        PhysicalFlowCreateCommand command = mapProposedFlowToPhysicalFlowCreateCommand(proposedFlow, logicalFlowId);

        LOG.info("User: {}, adding new physical flow: {}", username, command);
        try {
            return physicalFlowService.create(command, username);
        } catch (Exception ex) {
            LOG.error("Failed to create physical flow from proposedFlowId={}", proposedFlow.id(), ex);
            throw new FlowCreationException("Physical flow creation failed", ex);
        }
    }

    public ProposedFlowResponse proposedFlowAction(Long proposedFlowId,
                                                   ProposedFlowWorkflowTransitionAction transitionAction,
                                                   String username,
                                                   ProposedFlowActionCommand proposedFlowActionCommand) throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowResponse proposedFlowResponse = null;
        final ProposedFlowResponse proposedFlow = getProposedFlowById(proposedFlowId);

        // Check for approval/rejection permissions
        ProposeFlowPermission flowPermission = permissionService.checkUserPermission(
                username,
                mkRef(EntityKind.valueOf(proposedFlow.sourceEntityKind()), proposedFlow.sourceEntityId()),
                mkRef(EntityKind.valueOf(proposedFlow.targetEntityKind()), proposedFlow.targetEntityId())
        );
        boolean isSourceApprover = !flowPermission.sourceApprover().isEmpty();
        boolean isTargetApprover = !flowPermission.targetApprover().isEmpty();

//        Fetch the current state
        ProposedFlowWorkflowState currentState = valueOf(proposedFlow.workflowState().state());

        // 2. Get current state and build context
        ProposedFlowWorkflowContext workflowContext =
                new ProposedFlowWorkflowContext(
                        proposedFlow.workflowState().workflowId(),
                        proposedFlow.workflowState().entityReference(), username, proposedFlowActionCommand.comment())
                        .setSourceApprover(isSourceApprover)
                        .setTargetApprover(isTargetApprover)
                        .setCurrentState(currentState);

        try {
            // Fire the transitionAction
            ProposedFlowWorkflowState newState = proposedFlowStateMachine.fire(
                    currentState,
                    transitionAction,
                    workflowContext);

            // if the transition not found, not permitted or new state == current state happen, abort
            // Persist the new state.
            entityWorkflowService.updateStateTransition(username, proposedFlowActionCommand.comment(),
                    proposedFlow.workflowState(), currentState.name(), newState.name());

            LogicalPhysicalFlowCreationResponse response = null;
            ProposedFlowWorkflowState nextPossibleTransition = proposedFlowStateMachine
                    .nextPossibleTransition(
                            newState,
                            transitionAction,
                            workflowContext
                                    .setCurrentState(newState)
                                    .setPrevState(currentState))
                    .orElse(null);

            if (ProposedFlowWorkflowState.FULLY_APPROVED.equals(nextPossibleTransition)) {
                // auto switch to fully approved
                response = createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlowId, username);

                entityWorkflowService.updateStateTransition(username, proposedFlowActionCommand.comment(),
                        proposedFlow.workflowState(), newState.name(), nextPossibleTransition.name());
            }

            // Refresh Return Object
            EntityWorkflowView entityWorkflowView = entityWorkflowService.getEntityWorkflowView(
                    PROPOSE_FLOW_LIFECYCLE_WORKFLOW, proposedFlow.workflowState().entityReference());
            proposedFlowResponse = ImmutableProposedFlowResponse
                    .copyOf(proposedFlowResponse)
                    .withWorkflowState(entityWorkflowView.workflowState())
                    .withWorkflowTransitionList(entityWorkflowView.workflowTransitionList())
                    .withLogicalFlowId(response != null ? response.logicalFlow().id().get() : null)
                    .withPhysicalFlowId(response != null ? response.physicalFlowCreateCommandResponse().entityReference().id() : null);
        } catch (Exception e) {
            LOG.error("Error Occurred : {} ", e.getMessage());
            throw e;
        }

        return proposedFlowResponse;
    }

    public ProposeFlowPermission getUserPermissionsForEntityRef(String username, EntityReference entityRef) {
        if (EntityKind.PROPOSED_FLOW.equals(entityRef.kind())) {
            ProposedFlowResponse flowResponse = getProposedFlowById(entityRef.id());
            return permissionService.checkUserPermission(username,
                    mkRef(EntityKind.valueOf(flowResponse.sourceEntityKind()), flowResponse.sourceEntityId()),
                    mkRef(EntityKind.valueOf(flowResponse.targetEntityKind()), flowResponse.targetEntityId()));
        } else {
            throw new UnsupportedOperationException(format("%s is not supported", entityRef.kind()));
        }
    }
}
