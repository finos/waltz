package org.finos.waltz.service.proposed_flow_workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.ImmutableEntityWorkflowState;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.service.actor.ActorService;
import org.finos.waltz.service.data_flow.DataFlowService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.service.settings.SettingsService;
import org.finos.waltz.service.workflow_state_machine.WorkflowDefinition;
import org.finos.waltz.service.workflow_state_machine.WorkflowStateMachine;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionNotFoundException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionPredicateFailedException;
import org.finos.waltz.service.workflow_state_machine.exception.TransitionUpdateFailedException;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowContext;
import org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.finos.waltz.model.proposed_flow.ProposedFlowApprovers;


import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityKind.ACTOR;
import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.command.CommandOutcome.FAILURE;
import static org.finos.waltz.model.command.CommandOutcome.SUCCESS;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.*;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.*;


@Service
public class ProposedFlowWorkflowService {
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowWorkflowService.class);
    private static final String PROPOSED_FLOW_CREATED_WITH_SUCCESS = "PROPOSED_FLOW_CREATED_WITH_SUCCESS";
    private static final String PROPOSED_FLOW_CREATED_WITH_FAILURE = "PROPOSED_FLOW_CREATED_WITH_FAILURE";
    private static final String PROPOSED_FLOW_SUBMITTED = "Proposed Flow Submitted";
    private static final String PROPOSED_FLOW_ALREADY_EXIST = "Proposed Flow Already Exist";
    private static final String PHYSICAL_FLOW_ALREADY_EXIST = "Physical Flow Already Exist";
    private static final String PROPOSED_FLOW_ACTION_SUCCESS = "Proposed Flow Action success";
    private static final String TIME_OUT_REASON = "Auto cancelled; pending for more than %d days";
    private static final String AUTO_APPROVAL_REASON = "Auto approved for external actor";
    private static final String ADMIN = "Admin";
    private static final String AUTO_APPROVE_SETTING_KEY = "feature.auto-approve-flow-for-external-actors";
    private static final String PENDING_FLOWS_TIME_OUT_THRESHOLD = "feature.data-flows-timeout-threshold";

    private final EntityWorkflowService entityWorkflowService;
    private final ProposedFlowWorkflowPermissionService permissionService;
    private final DataFlowService dataFlowService;
    private final ProposedFlowDao proposedFlowDao;
    private final WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext>
            proposedFlowStateMachine;
    private final WorkflowDefinition proposedFlowWorkflowDefinition;
    private final ActorService actorService;
    private final SettingsService settingsService;
    private final TaskExecutor taskExecutor;


    @Autowired
    ProposedFlowWorkflowService(EntityWorkflowService entityWorkflowService,
                                ProposedFlowDao proposedFlowDao,
                                DSLContext dslContext,
                                WorkflowDefinition proposedFlowWorkflowDefinition,
                                ProposedFlowWorkflowPermissionService permissionService,
                                DataFlowService dataFlowService,
                                ActorService actorService,
                                SettingsService settingsService,
                                TaskExecutor taskExecutor) {
        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(proposedFlowWorkflowDefinition, "proposedFlowWorkflowDefinition cannot be null");
        checkNotNull(permissionService, "ProposedFlowWorkflowPermissionService cannot be null");


        this.entityWorkflowService = entityWorkflowService;
        this.proposedFlowDao = proposedFlowDao;
        this.proposedFlowWorkflowDefinition = proposedFlowWorkflowDefinition;
//        Get the state machine from the definition
        this.proposedFlowStateMachine = proposedFlowWorkflowDefinition.getMachine();
        this.permissionService = permissionService;
        this.dataFlowService = dataFlowService;
        this.actorService = actorService;
        this.settingsService = settingsService;
        this.taskExecutor = taskExecutor;
    }

    public ProposedFlowCommandResponse proposeNewFlow(String username, ProposedFlowCommand proposedFlowCommand) {
        EntityWorkflowDefinition workflowDefinition = entityWorkflowService.searchByName(ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        FlowIdResponse flowIdResponse = validateProposedFlow(proposedFlowCommand, username);
        return flowIdResponse == null ? createProposedFlow(username, proposedFlowCommand, workflowDefinition) : getDuplicateFlowResponse(proposedFlowCommand, flowIdResponse, workflowDefinition);
    }

    private ProposedFlowCommandResponse createProposedFlow(String username, ProposedFlowCommand proposedFlowCommand, EntityWorkflowDefinition workflowDefinition) {
        String msg = PROPOSED_FLOW_CREATED_WITH_SUCCESS;
        CommandOutcome outcome = SUCCESS;
        Long proposedFlowId = null;
        try {
            proposedFlowId = proposedFlowDao.saveProposedFlow(username, proposedFlowCommand);
            EntityReference proposedFlowRef = mkRef(PROPOSED_FLOW, proposedFlowId);
            workflowDefinition = entityWorkflowService.searchByName(ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW);

            // Get current state and build context
            ProposedFlowWorkflowContext workflowContext = new ProposedFlowWorkflowContext(
                    workflowDefinition.id().get(),
                    proposedFlowRef, username, proposedFlowCommand.reason().description());

            // Fire the action
            ProposedFlowWorkflowState newState = proposedFlowStateMachine.fire(PROPOSED_CREATE, PROPOSE, workflowContext);
            entityWorkflowService.createEntityWorkflow(proposedFlowRef, workflowDefinition.id().get(),
                    username, PROPOSED_FLOW_SUBMITTED, PROPOSED_CREATE.name(), newState.name(), proposedFlowCommand.reason().description());

            doAutoApprovals(proposedFlowRef,proposedFlowCommand,username);
        } catch (Exception e) {
            msg = PROPOSED_FLOW_CREATED_WITH_FAILURE;
            outcome = FAILURE;
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

    private void doAutoApprovals(EntityReference proposedFlowRef, ProposedFlowCommand proposedFlowCommand, String username) {
        taskExecutor.execute(() -> {
            try {
                ProposedFlowResponse proposedFlow = getProposedFlowResponseById(proposedFlowRef.id());
                proposedFlow = autoApproveFlowsForExternalActors(proposedFlow,proposedFlowCommand);
                proposedFlow = autoApproveWhenProposerIsApprover(proposedFlow,username);
            } catch (Exception e) {
                LOG.error("Unable to auto approve the proposed flow id={}", proposedFlowRef.id(), e);
            }
        });
    }

    private ProposedFlowResponse autoApproveFlowsForExternalActors(ProposedFlowResponse proposedFlow, ProposedFlowCommand proposedFlowCommand) throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {
        if (isAutoApproveEnabledForExternalActors()) {
            return approveForExternalActor(
                    proposedFlow,
                    ADMIN,
                    proposedFlowCommand);
        }
        return proposedFlow;
    }

    private ProposedFlowResponse autoApproveWhenProposerIsApprover(ProposedFlowResponse proposedFlow, String username) {

        ProposeFlowPermission flowPermission = permissionService.checkUserPermission(
                username,
                proposedFlow.flowDef().source(),
                proposedFlow.flowDef().target()
        );

        if(!flowPermission.sourceApprover().isEmpty()) {
            ProposedFlowActionCommand proposedFlowActionCommand = ImmutableProposedFlowActionCommand.builder()
                    .comment("Approved as proposer is approver for source")
                    .build();
            proposedFlow = proposedFlowAction(proposedFlow, APPROVE, flowPermission, username, proposedFlowActionCommand);
        }

        if(!flowPermission.targetApprover().isEmpty()) {
            ProposedFlowActionCommand proposedFlowActionCommand = ImmutableProposedFlowActionCommand.builder()
                    .comment("Approved as proposer is approver for target")
                    .build();
            proposedFlow = proposedFlowAction(proposedFlow, APPROVE, flowPermission, username, proposedFlowActionCommand);
        }
        return proposedFlow;
    }

    private ProposedFlowResponse approveForExternalActor(ProposedFlowResponse proposedFlow, String username, ProposedFlowCommand proposedFlowCommand) {
        boolean isSourceExternalActor = isExternalActor(proposedFlowCommand.source());
        boolean isTargetExternalActor = isExternalActor(proposedFlowCommand.target());

        if (!isSourceExternalActor && !isTargetExternalActor) {
            return  proposedFlow;
        }

        ProposeFlowPermission flowPermission = ImmutableProposeFlowPermission.builder()
                .sourceApprover(isSourceExternalActor ? Set.of(Operation.APPROVE) : Set.of())
                .targetApprover(isTargetExternalActor ? Set.of(Operation.APPROVE) : Set.of())
                .build();

        ProposedFlowActionCommand proposedFlowActionCommand = ImmutableProposedFlowActionCommand.builder()
                .comment(AUTO_APPROVAL_REASON)
                .build();

        if (isSourceExternalActor) {
            proposedFlow = proposedFlowAction(proposedFlow, APPROVE, flowPermission, username, proposedFlowActionCommand);
        }

        if (isTargetExternalActor) {
            proposedFlow = proposedFlowAction(proposedFlow, APPROVE, flowPermission, username, proposedFlowActionCommand);
        }
        return proposedFlow;
    }

    private boolean isAutoApproveEnabledForExternalActors() {
        return settingsService
                .getValue(AUTO_APPROVE_SETTING_KEY)
                .map(Boolean::valueOf)
                .orElse(false);
    }

    private boolean isExternalActor(ProposedFlowEntityReference entityReference) {
        if (entityReference == null || entityReference.kind() != ACTOR) {
            return false;
        }

        Actor actor = actorService.getById(entityReference.id());
        return actor != null && actor.isExternal();
    }

    private ProposedFlowCommandResponse getDuplicateFlowResponse(ProposedFlowCommand command, FlowIdResponse response,
                                                                 EntityWorkflowDefinition workflowDefinition) {
        ImmutableProposedFlowCommandResponse.Builder builder = ImmutableProposedFlowCommandResponse.builder();
        if (response.type().equals(PROPOSED_FLOW)) {
            builder.message(PROPOSED_FLOW_ALREADY_EXIST)
                    .proposedFlowId(response.id());
        } else {
            builder.message(PHYSICAL_FLOW_ALREADY_EXIST)
                    .physicalFlowId(response.id())
                    .proposedFlowId(0L);
        }
        return builder
                .outcome(FAILURE)
                .proposedFlowCommand(command)
                .workflowDefinitionId(workflowDefinition != null ? workflowDefinition.id().get() : null)
                .build();

    }


    public List<ProposedFlowResponse> getProposedFlows(IdSelectionOptions options) throws JsonProcessingException {
        EntityWorkflowDefinition workflowDefinition = entityWorkflowService.searchByName(ProposedFlowDao.PROPOSE_FLOW_LIFECYCLE_WORKFLOW);
        return proposedFlowDao.getProposedFlowsByUser(
                options.entityReference().id(),
                options.scope() == CHILDREN ? true : false,
                workflowDefinition.id().get()
        );
    }

    public ProposedFlowResponse getProposedFlowResponseById(long id) {
        return proposedFlowDao.getProposedFlowResponseById(id);
    }

    public ProposedFlowResponse proposedFlowAction(ProposedFlowResponse proposedFlow,
                                                   ProposedFlowWorkflowTransitionAction transitionAction,
                                                   ProposeFlowPermission flowPermission,
                                                   String username,
                                                   ProposedFlowActionCommand proposedFlowActionCommand) {
        String errorMessage = PROPOSED_FLOW_ACTION_SUCCESS;
        CommandOutcome outcome = SUCCESS;

        boolean isSourceApprover = !flowPermission.sourceApprover().isEmpty();
        boolean isTargetApprover = !flowPermission.targetApprover().isEmpty();
        boolean isMaker = proposedFlow.createdBy().equalsIgnoreCase(username);

//        Fetch the current state
        ProposedFlowWorkflowState currentState = ProposedFlowWorkflowState.valueOf(proposedFlow.workflowState().state());

        // 2. Get current state and build context
        ProposedFlowWorkflowContext workflowContext =
                new ProposedFlowWorkflowContext(
                        proposedFlow.workflowState().workflowId(),
                        proposedFlow.workflowState().entityReference(), username, proposedFlowActionCommand.comment())
                        .setSourceApprover(isSourceApprover)
                        .setTargetApprover(isTargetApprover)
                        .setMaker(isMaker)
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
                proposedFlowOperations(proposedFlow, username);
                proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlow.id());

                EntityWorkflowState refreshedWorkflowState = entityWorkflowService.getStateForEntityReferenceAndWorkflowId(
                        proposedFlow.workflowState().workflowId(),
                        proposedFlow.workflowState().entityReference());

                entityWorkflowService.updateStateTransition(username, proposedFlowActionCommand.comment(),
                        refreshedWorkflowState, newState.name(), nextPossibleTransition.name());
            }

            // Refresh Return Object
            proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlow.id());

        } catch (TransitionPredicateFailedException e) {
            errorMessage = String.format("%s Failed. The workflow may have been updated or you no longer have permissions to %s this item.", transitionAction, transitionAction.getVerb());
            LOG.error(errorMessage, e);
            outcome = FAILURE;
        } catch (TransitionUpdateFailedException e) {
            errorMessage = String.format("Failed to '%s' proposed flow. The workflow may have updated.", transitionAction);
            LOG.error(errorMessage, e);
            outcome = FAILURE;
        } catch (Exception e) {
            errorMessage = String.format("Failed to '%s' proposed flow.", transitionAction);
            LOG.error(errorMessage, e);
            outcome = FAILURE;
        }

        return ImmutableProposedFlowResponse.builder()
                .from(proposedFlow)
                .outcome(outcome)
                .message(errorMessage)
                .build();
    }

    public ProposedFlowResponse proposedFlowAction(Long proposedFlowId,
                                                   ProposedFlowWorkflowTransitionAction transitionAction,
                                                   String username,
                                                   ProposedFlowActionCommand proposedFlowActionCommand) {
        ProposedFlowResponse proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlowId);
        checkNotNull(proposedFlow, "No proposed flow found");

        // Check for approval/rejection permissions
        ProposeFlowPermission flowPermission = permissionService.checkUserPermission(
                username,
                proposedFlow.flowDef().source(),
                proposedFlow.flowDef().target()
        );

        return proposedFlowAction(proposedFlow, transitionAction, flowPermission, username, proposedFlowActionCommand);
    }


    public ProposeFlowPermission getUserPermissionsForEntityRef(String username, EntityReference entityRef) {
        if (PROPOSED_FLOW.equals(entityRef.kind())) {
            ProposedFlowResponse flowResponse = proposedFlowDao.getProposedFlowResponseById(entityRef.id());
            return permissionService.checkUserPermission(username,
                    flowResponse.flowDef().source(),
                    flowResponse.flowDef().target()
            );
        } else {
            throw new UnsupportedOperationException(format("%s is not supported", entityRef.kind()));
        }
    }

    /**
     * @param proposedFlowCommand
     * @param username
     * @return FlowIdResponse having id and type based on the matched case
     * type can be physical flow or proposed flow
     * if already exist else returns null
     */
    public FlowIdResponse validateProposedFlow(ProposedFlowCommand proposedFlowCommand, String username) {

        switch (proposedFlowCommand.proposalType()) {
            case CREATE:
                return validateProposedFlowForCreate(proposedFlowCommand, username);
            case EDIT:
                return validateProposedFlowForEdit(proposedFlowCommand);
            case DELETE:
                return validateProposedFlowForDelete(proposedFlowCommand);
            default:
                throw new UnsupportedOperationException(
                        "proposalType not supported: " + proposedFlowCommand.proposalType()
                );
        }
    }

    public boolean isAppInvolvedInPendingApprovals(EntityReference appRef, String username, Long workflowId) {
        return proposedFlowDao.isAppInvolvedInPendingApprovals(appRef, username, workflowId);
    }

    public boolean hasPendingCreations(EntityReference appRef, Long workflowId) {
        return proposedFlowDao.hasPendingCreations(appRef, workflowId);
    }

    public Set<Long> findPhysicalFlowIdsInPendingProposals(Set<Long> logicalFlowIds, Long workflowId) {
        return proposedFlowDao.findPhysicalFlowIdsInPendingProposals(logicalFlowIds, workflowId);
    }

    private void proposedFlowOperations(ProposedFlowResponse proposedFlow, String username) throws FlowCreationException {
        switch (proposedFlow.flowDef().proposalType()) {
            case CREATE:
                dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlow.id(), username);
                break;
            case EDIT:
                dataFlowService.editPhysicalFlow(proposedFlow, username);
                break;
            case DELETE:
                dataFlowService.deletePhysicalFlow(proposedFlow, username);
                break;
            default:
                throw new UnsupportedOperationException(
                        "proposalType not supported: " + proposedFlow.flowDef().proposalType()
                );
        }
    }

    private FlowIdResponse validateProposedFlowForCreate(ProposedFlowCommand command, String username) {
        return command.logicalFlowId()
                .map(id -> dataFlowService.getPhysicalFlowIfExist(command, username))
                .filter(Objects::nonNull)
                .map(flowId -> buildFlowIdResponse(flowId, EntityKind.PHYSICAL_FLOW))
                .orElseGet(() -> proposedFlowDao.proposedFlowRecordsByProposalType(command)
                        .stream()
                        .filter(record -> haveSamePhysicalAttributes(command, getFlowDefinition(record)))
                        .findFirst()
                        .map(proposedFlowRecord -> buildFlowIdResponse(proposedFlowRecord.getId(), PROPOSED_FLOW))
                        .orElse(null));
    }

    private FlowIdResponse buildFlowIdResponse(Long id, EntityKind flowType) {
        return ImmutableFlowIdResponse.builder()
                .id(id)
                .type(flowType)
                .build();
    }

    private FlowIdResponse validateProposedFlowForEdit(ProposedFlowCommand command) {
        checkNotNull(command.logicalFlowId(), "logical flow id can not be null");
        checkNotNull(command.physicalFlowId(), "physical flow id can not be null");
        checkNotEmpty(command.dataTypeIds(), "dataTypeIds can not be empty");

        return proposedFlowDao.proposedFlowRecordsByProposalType(command)
                .stream()
                .filter(record ->
                        Objects.equals(record.getLogicalFlowId(), command.logicalFlowId().orElse(null))
                                && Objects.equals(record.getPhysicalFlowId(), command.physicalFlowId().orElse(null)))
                .findFirst()
                .map(proposedFlowRecord -> buildFlowIdResponse(proposedFlowRecord.getId(), PROPOSED_FLOW))
                .orElse(null);
    }

    private FlowIdResponse validateProposedFlowForDelete(ProposedFlowCommand command) {
        checkNotNull(command.physicalFlowId().get(), "physical flow id can not be null");
        checkNotNull(command.logicalFlowId().get(), "logical flow id can not be null");

        return proposedFlowDao.proposedFlowRecordsByProposalType(command)
                .stream()
                .filter(record ->
                        Objects.equals(record.getLogicalFlowId(), command.logicalFlowId().orElse(null))
                                && Objects.equals(record.getPhysicalFlowId(), command.physicalFlowId().orElse(null)))
                .findFirst()
                .map(proposedFlowRecord -> buildFlowIdResponse(proposedFlowRecord.getId(), PROPOSED_FLOW))
                .orElse(null);
    }

    private ProposedFlowCommand getFlowDefinition(ProposedFlowRecord record) {
        try {
            return getJsonMapper()
                    .readValue(record.getFlowDef(), ProposedFlowCommand.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }

    }

    public List<Long> fetchPendingActionFlowsForPersonWhereSourceOrTargetApprover(Long personId) {
        return proposedFlowDao.fetchPendingActionFlowsForPersonWhereSourceOrTargetApprover(personId);
    }

    /**
     * Compares two proposed flow commands to see if their key physical attributes are identical.
     *
     * @param newProposal      The new proposal being submitted.
     * @param existingProposal An existing proposal from the database.
     * @return True if the specification ID and core flow attributes (offset, frequency, transport, criticality) match.
     */
    private boolean haveSamePhysicalAttributes(ProposedFlowCommand newProposal, ProposedFlowCommand existingProposal) {

        boolean specIdMatches = Objects.equals(
                existingProposal.specification().id(),
                newProposal.specification().id());

        // Compare physical flow attributes to see if they are the same
        boolean attributesMatch = Objects.equals(
                existingProposal.flowAttributes().name(),
                newProposal.flowAttributes().name())
                && Objects.equals(
                existingProposal.flowAttributes().basisOffset(),
                newProposal.flowAttributes().basisOffset())
                && Objects.equals(
                existingProposal.flowAttributes().frequency(),
                newProposal.flowAttributes().frequency())
                && Objects.equals(
                existingProposal.flowAttributes().transport(),
                newProposal.flowAttributes().transport())
                && Objects.equals(
                existingProposal.flowAttributes().criticality(),
                newProposal.flowAttributes().criticality());

        return specIdMatches && attributesMatch;
    }

    public void timeOutPendingFlows() {

        int timeoutDays = getTimeoutDays(PENDING_FLOWS_TIME_OUT_THRESHOLD);

        LOG.info("Running pending proposed flow timeout job with timeoutDays={}", timeoutDays);

        long processedCount = autoTimeoutPendingProposedFlows(timeoutDays, ADMIN, format(TIME_OUT_REASON, timeoutDays));

        LOG.info("Completed pending proposed flow timeout job. processedCount={}", processedCount);
    }

    private int getTimeoutDays(String settingName) {
        return settingsService
                .getValue(settingName)
                .map(Integer::valueOf)
                .orElse(30);
    }

    private long autoTimeoutPendingProposedFlows(int timeoutDays,
                                                 String username,
                                                 String reason) {

        List<Long> proposedFlowIds = proposedFlowDao.findPendingFlowsOlderThanDays(timeoutDays);

        LOG.info("Fetched pending flows for more than 30 days. countOfFlows={}", proposedFlowIds.size());

        if (proposedFlowIds.isEmpty()) {
            return 0;
        }

        List<EntityWorkflowState> workflowStates = new ArrayList<>();
        List<String> currentStates = new ArrayList<>();

        List<ProposedFlowResponse> proposedFlowResponses = proposedFlowDao.getProposedFlowsForTimeout(proposedFlowIds);

        for (int i = 0; i < proposedFlowIds.size(); i++) {
            try {
                ProposedFlowResponse proposedFlow = proposedFlowResponses.get(i);
                if (proposedFlow == null || proposedFlow.workflowState() == null) {
                    LOG.warn("Skipping timeout for proposed flow {} because workflow state was not found", proposedFlowIds.get(i));
                    continue;
                }

                ProposedFlowWorkflowState currentState = ProposedFlowWorkflowState.valueOf(proposedFlow.workflowState().state());
                ProposedFlowWorkflowContext workflowContext = new ProposedFlowWorkflowContext(
                        proposedFlow.workflowState().workflowId(),
                        proposedFlow.workflowState().entityReference(),
                        username,
                        reason)
                        .setCurrentState(currentState);

                ProposedFlowWorkflowState newState = proposedFlowStateMachine.fire(
                        currentState,
                        TIME_OUT,
                        workflowContext);

                workflowStates.add(ImmutableEntityWorkflowState.copyOf(proposedFlow.workflowState()).withState(newState.name()));
                currentStates.add(currentState.name());
            } catch (TransitionPredicateFailedException e) {
                LOG.warn("Skipping timeout for proposed flow {} due to transition predicate failure", proposedFlowIds.get(i), e);
            } catch (Exception e) {
                LOG.error("Failed to auto-timeout proposed flow {}", proposedFlowIds.get(i), e);
            }
        }
        if (workflowStates.isEmpty()) {
            return 0;
        }

        try {
            return entityWorkflowService.updateStateTransition(username, reason, workflowStates, currentStates, TIMED_OUT.name());
        } catch (TransitionUpdateFailedException e) {
            LOG.error("Failed to persist auto-timeout batch for {} proposed flows", workflowStates.size(), e);
            return 0;
        }
    }

    public ProposedFlowApprovers getApprovers(long proposedFlowId) {
        // 1. It receives a List<ApproverWithType>.
        List<ApproverWithType> allApproversFromDao = proposedFlowDao.findApproversForProposedFlow(proposedFlowId);

        // 2. Partition the single list into two lists based on the 'approverType'
        Map<String, List<ApproverWithType>> partitionedApprovers = allApproversFromDao
                .stream()
                .collect(Collectors.groupingBy(
                        ApproverWithType::approverType));
        // 3. NEW: Map the internal DTOs to the final response DTOs
        List<ProposedFlowApprover> sourceApprovers = partitionedApprovers
                .getOrDefault("SOURCE", emptyList())
                .stream()
                .map(daoApprover -> ImmutableProposedFlowApprover.builder()
                        .person(daoApprover.person())
                        .involvementKindId(daoApprover.involvementKindId())
                        .involvementKindName(daoApprover.involvementKindName())
                        .build())
                .collect(Collectors.toList());

        List<ProposedFlowApprover> targetApprovers = partitionedApprovers
                .getOrDefault("TARGET", emptyList())
                .stream()
                .map(daoApprover -> ImmutableProposedFlowApprover.builder()
                        .person(daoApprover.person())
                        .involvementKindId(daoApprover.involvementKindId())
                        .involvementKindName(daoApprover.involvementKindName())
                        .build())
                .collect(Collectors.toList());

        // 4. Build the final structured response object
        return ImmutableProposedFlowApprovers.builder()
                .sourceApprovers(sourceApprovers)
                .targetApprovers(targetApprovers)
                .build();
    }
}
