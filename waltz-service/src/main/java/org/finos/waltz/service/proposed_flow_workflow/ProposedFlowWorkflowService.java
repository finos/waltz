package org.finos.waltz.service.proposed_flow_workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.common.exception.FlowCreationException;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.service.data_flow.DataFlowService;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
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
import java.util.Objects;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.model.EntityKind.PROPOSED_FLOW;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.HierarchyQueryScope.CHILDREN;
import static org.finos.waltz.model.command.CommandOutcome.FAILURE;
import static org.finos.waltz.model.command.CommandOutcome.SUCCESS;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.PROPOSED_CREATE;
import static org.finos.waltz.model.proposed_flow.ProposedFlowWorkflowState.valueOf;
import static org.finos.waltz.service.workflow_state_machine.proposed_flow.ProposedFlowWorkflowTransitionAction.PROPOSE;


@Service
public class ProposedFlowWorkflowService {
    private static final Logger LOG = LoggerFactory.getLogger(ProposedFlowWorkflowService.class);
    private static final String PROPOSED_FLOW_CREATED_WITH_SUCCESS = "PROPOSED_FLOW_CREATED_WITH_SUCCESS";
    private static final String PROPOSED_FLOW_CREATED_WITH_FAILURE = "PROPOSED_FLOW_CREATED_WITH_FAILURE";
    private static final String PROPOSED_FLOW_SUBMITTED = "Proposed Flow Submitted";
    private static final String PROPOSED_FLOW_ALREADY_EXIST = "Proposed Flow Already Exist";
    private static final String PHYSICAL_FLOW_ALREADY_EXIST = "Physical Flow Already Exist";

    private final EntityWorkflowService entityWorkflowService;
    private final ProposedFlowWorkflowPermissionService permissionService;
    private final DataFlowService dataFlowService;
    private final ProposedFlowDao proposedFlowDao;
    private final WorkflowDefinition proposedFlowWorkflowDefinition;
    private final WorkflowStateMachine<ProposedFlowWorkflowState, ProposedFlowWorkflowTransitionAction, ProposedFlowWorkflowContext>
            proposedFlowStateMachine;

    @Autowired
    ProposedFlowWorkflowService(EntityWorkflowService entityWorkflowService,
                                ProposedFlowDao proposedFlowDao,
                                DSLContext dslContext,
                                WorkflowDefinition proposedFlowWorkflowDefinition,
                                ProposedFlowWorkflowPermissionService permissionService,
                                DataFlowService dataFlowService) {
        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(proposedFlowWorkflowDefinition, "proposedFlowWorkflowDefinition cannot be null");
        checkNotNull(permissionService, "ProposedFlowWorkflowPermissionService cannot be null");


        this.entityWorkflowService = entityWorkflowService;
        this.proposedFlowDao = proposedFlowDao;
        this.proposedFlowWorkflowDefinition = proposedFlowWorkflowDefinition;
//        Get the state machine from the definition
        proposedFlowStateMachine = proposedFlowWorkflowDefinition.getMachine();
        this.permissionService = permissionService;
        this.dataFlowService = dataFlowService;
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

    private ProposedFlowCommandResponse getDuplicateFlowResponse(ProposedFlowCommand command, FlowIdResponse response,
                                                                 EntityWorkflowDefinition workflowDefinition) {
        ImmutableProposedFlowCommandResponse.Builder builder = ImmutableProposedFlowCommandResponse.builder();
        if(response.type().equals(PROPOSED_FLOW)) {
            builder.message(PROPOSED_FLOW_ALREADY_EXIST)
                    .proposedFlowId(response.id());
        } else  {
            builder.message(PHYSICAL_FLOW_ALREADY_EXIST)
                    .physicalFlowId(response.id())
                    .proposedFlowId(0L);
        }
        return  builder
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

    public ProposedFlowResponse proposedFlowAction(Long proposedFlowId,
                                                   ProposedFlowWorkflowTransitionAction transitionAction,
                                                   String username,
                                                   ProposedFlowActionCommand proposedFlowActionCommand) throws FlowCreationException, TransitionNotFoundException, TransitionPredicateFailedException {
        ProposedFlowResponse proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlowId);
        checkNotNull(proposedFlow, "No proposed flow found");

        // Check for approval/rejection permissions
        ProposeFlowPermission flowPermission = permissionService.checkUserPermission(
                username,
                proposedFlow.flowDef().source(),
                proposedFlow.flowDef().target()
        );
        boolean isSourceApprover = !flowPermission.sourceApprover().isEmpty();
        boolean isTargetApprover = !flowPermission.targetApprover().isEmpty();
        boolean isMaker = proposedFlow.createdBy().equalsIgnoreCase(username);

//        Fetch the current state
        ProposedFlowWorkflowState currentState = valueOf(proposedFlow.workflowState().state());

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

                entityWorkflowService.updateStateTransition(username, proposedFlowActionCommand.comment(),
                        proposedFlow.workflowState(), newState.name(), nextPossibleTransition.name());
            }

            // Refresh Return Object
            proposedFlow = proposedFlowDao.getProposedFlowResponseById(proposedFlowId);
        } catch (Exception e) {
            LOG.error("Error Occurred : {} ", e.getMessage());
            throw e;
        }

        return proposedFlow;
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
     *
     * @param proposedFlowCommand
     * @param username
     * @return FlowIdResponse having id and type based on the matched case
     * type can be physical flow or proposed flow
     * if already exist else returns null
     */
    public FlowIdResponse validateProposedFlow(ProposedFlowCommand proposedFlowCommand, String username) {

        switch (proposedFlowCommand.proposalType()){
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

    private void proposedFlowOperations(ProposedFlowResponse proposedFlow, String username) throws FlowCreationException {
        switch (proposedFlow.flowDef().proposalType()) {
            case CREATE:
                dataFlowService.createLogicalAndPhysicalFlowFromProposedFlowDef(proposedFlow.id(), username);
                break;
            case EDIT:
                dataFlowService.editPhysicalFlow(proposedFlow, username);
                break;
            case DELETE:
                dataFlowService.deletePhysicalFlow(proposedFlow.physicalFlowId(), username);
                break;
            default:
                throw new UnsupportedOperationException(
                        "proposalType not supported: " + proposedFlow.flowDef().proposalType()
                );
        }
    }

    private FlowIdResponse validateProposedFlowForCreate(ProposedFlowCommand command, String username){
        return command.logicalFlowId()
                .map(id -> dataFlowService.getPhysicalFlowIfExist(command, username))
                .filter(Objects::nonNull)
                .map(flowId -> buildFlowIdResponse(flowId, EntityKind.PHYSICAL_FLOW))
                .orElseGet(() -> proposedFlowDao.proposedFlowRecordsByProposalType(command)
                        .stream()
                        .findFirst()
                        .map(proposedFlowRecord -> buildFlowIdResponse(proposedFlowRecord.getId(), PROPOSED_FLOW))
                        .orElse(null));
    }

    private FlowIdResponse buildFlowIdResponse(Long id, EntityKind flowType){
        return ImmutableFlowIdResponse.builder()
                .id(id)
                .type(flowType)
                .build();
    }

    private FlowIdResponse validateProposedFlowForEdit(ProposedFlowCommand command){
        checkNotNull(command.logicalFlowId().get(),"logical flow id can not be null");
        checkNotNull(command.physicalFlowId().get(),"physical flow id can not be null");
        checkNotEmpty(command.dataTypeIds(), "dataTypeIds can not be empty");

        return proposedFlowDao.proposedFlowRecordsByProposalType(command)
                .stream()
                .filter(record -> {
                    ProposedFlowCommand flow = getFlowDefinition(record);
                    return flow.logicalFlowId().isPresent() && flow.physicalFlowId().isPresent()
                            && flow.logicalFlowId().get().equals(command.logicalFlowId().orElse(null))
                            && flow.physicalFlowId().get().equals(command.physicalFlowId().orElse(null));
                })
                .findFirst()
                .map(proposedFlowRecord -> buildFlowIdResponse(proposedFlowRecord.getId(), PROPOSED_FLOW))
                .orElse(null);
    }

    private FlowIdResponse validateProposedFlowForDelete(ProposedFlowCommand command){
        checkNotNull(command.physicalFlowId().get(),"physical flow id can not be null");
        checkNotNull(command.logicalFlowId().get(),"logical flow id can not be null");

        return proposedFlowDao.proposedFlowRecordsByProposalType(command)
                .stream()
                .filter(record -> {
                    ProposedFlowCommand flow = getFlowDefinition(record);
                    return flow.logicalFlowId().isPresent() && flow.physicalFlowId().isPresent()
                            && flow.logicalFlowId().get().equals(command.logicalFlowId().orElse(null))
                            && flow.physicalFlowId().get().equals(command.physicalFlowId().orElse(null));
                })
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

}
