package org.finos.waltz.service.maker_checker;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.data.changelog.ChangeLogDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.proposed_flow.*;
import org.finos.waltz.schema.tables.records.ProposedFlowRecord;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
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
import java.util.concurrent.atomic.AtomicReference;

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
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final ProposedFlowDao proposedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;
    private final DSLContext dslContext;
    private final ChangeLogDao changeLogDao;
    private final WorkflowDefinition proposedFlowWorkflowDefinition;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        ProposedFlowDao proposedFlowDao,
                        DSLContext dslContext,
                        ChangeLogDao changeLogDao,
                        WorkflowDefinition proposedFlowWorkflowDefinition) {
        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(entityWorkflowStateDao, "entityWorkflowStateDao cannot be null");
        checkNotNull(entityWorkflowTransitionDao, "entityWorkflowTransitionDao cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(changeLogDao, "changeLogDao cannot be null");
        checkNotNull(proposedFlowWorkflowDefinition, "proposedFlowWorkflowDefinition cannot be null");

        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.proposedFlowDao = proposedFlowDao;
        this.dslContext = dslContext;
        this.changeLogDao = changeLogDao;
        this.proposedFlowWorkflowDefinition = proposedFlowWorkflowDefinition;
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
                    .build();
        } catch (JsonProcessingException e) {
            LOG.error("Invalid flow definition JSON : {} ", e.getMessage());
            throw new IllegalArgumentException("Invalid flow definition JSON", e);
        }
    }
}
