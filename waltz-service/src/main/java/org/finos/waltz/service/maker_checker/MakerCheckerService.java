package org.finos.waltz.service.maker_checker;

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
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommandResponse;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.jooq.DSLContext;
import org.jooq.exception.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityReference.mkRef;


@Service
public class MakerCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final ProposedFlowDao proposedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;
    private final DSLContext dslContext;

    private final ChangeLogDao changeLogDao;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        ProposedFlowDao proposedFlowDao,
                        DSLContext dslContext,
                        ChangeLogDao changeLogDao){

        checkNotNull(entityWorkflowService, "entityWorkflowService cannot be null");
        checkNotNull(entityWorkflowStateDao, "entityWorkflowStateDao cannot be null");
        checkNotNull(entityWorkflowTransitionDao, "entityWorkflowTransitionDao cannot be null");
        checkNotNull(proposedFlowDao, "proposedFlowDao cannot be null");
        checkNotNull(dslContext, "dslContext cannot be null");
        checkNotNull(changeLogDao, "changeLogDao cannot be null");

        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.proposedFlowDao = proposedFlowDao;
        this.dslContext = dslContext;
        this.changeLogDao = changeLogDao;
    }

    public ProposedFlowCommandResponse proposeNewFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand){

        AtomicReference<Long> proposedFlowId = new AtomicReference<>(-1L);
        String msg = "PROPOSE_FLOW_CREATED_WITH_SUCCESS";
        String outcome = CommandOutcome.SUCCESS.name();
        try {
            dslContext.transaction(dslContext ->{
                DSLContext dsl = dslContext.dsl();
                proposedFlowId.set(proposedFlowDao.saveProposedFlow(requestBody, username, proposedFlowCommand));
                LOG.info("New ProposedFlowId is : {} ", proposedFlowId);
                EntityWorkflowDefinition ewd = entityWorkflowService.searchByName("Propose Flow Lifecycle Workflow");
                if(ewd.id().isPresent()){
                    entityWorkflowStateDao.saveNewWorkflowState(proposedFlowId.get(), ewd.id().get(), username);
                    entityWorkflowTransitionDao.saveNewWorkflowTransition(proposedFlowId.get(), ewd.id().get(), username);
                }else{
                    throw new NoDataFoundException("Could not find workflow definition: Propose Flow Lifecycle Workflow");
                }

                List<ChangeLog> changeLogList = new ArrayList<>();
                changeLogList.add(createChangeLogObject("New Proposed Flow Created", username, proposedFlowId.get()));
                changeLogList.add(createChangeLogObject("Entity Workflow State changed to Submitted", username, proposedFlowId.get()));
                changeLogList.add(createChangeLogObject("Entity Workflow Transition saved with from: Proposed-Create to: Action-Pending State", username, proposedFlowId.get()));
                changeLogDao.write(changeLogList);

            });
        }
        catch (Exception e){
            msg = "PROPOSE_FLOW_CREATED_WITH_FAILURE";
            outcome = CommandOutcome.FAILURE.name();
            LOG.info("Error Occurred : {} ", e.getMessage());
            e.printStackTrace();
        }
        return ImmutableProposedFlowCommandResponse.builder()
                .message(msg)
                .outcome(outcome)
                .proposedFlowCommand(proposedFlowCommand)
                .proposedFlowId(proposedFlowId.get())
                .build();
    }

    public ChangeLog createChangeLogObject(String msg, String userName, Long proposedFlowId){
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
     * Service-layer façade: delegates to DAO and keeps business rules here.
     *
     * @param id the primary key of the flow
     * @return JSON string if the row exists, otherwise empty
     */
    public Optional<String> getFlowDefinition(long id) {
        // Any additional business logic can be added here
        return proposedFlowDao.findFlowDefById(id);
    }
}
