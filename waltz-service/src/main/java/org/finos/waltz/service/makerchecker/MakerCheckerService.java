package org.finos.waltz.service.makerchecker;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class MakerCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final ProposedFlowDao proposedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    private final DSLContext dslContext;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        ProposedFlowDao proposedFlowDao,
                        DSLContext dslContext){
        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.proposedFlowDao = proposedFlowDao;
        this.dslContext = dslContext;
    }

    public ProposedFlowCommandResponse proposedNewFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand){

        AtomicReference<Long> proposedFlowId = new AtomicReference<>(-1L);
        String msg = "FLOW_CREATED";
        String outcome = "SUCCESS";
        try {
            dslContext.transaction(dslContext ->{
                DSLContext dsl = dslContext.dsl();
                proposedFlowId.set(proposedFlowDao.saveRequestedFlow(requestBody, username, proposedFlowCommand));
                LOG.info("New ProposedFlowId is : {} ", proposedFlowId);
                EntityWorkflowDefinition ewd = entityWorkflowService.searchByName("Requested Flow Lifecycle Workflow");
                if(ewd.id().isPresent()){
                    entityWorkflowStateDao.saveNewWorkflowState(proposedFlowId.get(), ewd.id().get(), username);
                    entityWorkflowTransitionDao.saveNewWorkflowTransition(proposedFlowId.get(), ewd.id().get(), username);
                }else{
                    throw new NoDataFoundException("Could not find workflow definition: Requested Flow Lifecycle Workflow");
                }
            });
        }
        catch (Exception e){
            msg = "FLOW_NOT_CREATED";
            outcome = "FAILURE";
            LOG.info("Error Occurred : {} ", e.getMessage());
        }
        return ImmutableProposedFlowCommandResponse.builder()
                .message(msg)
                .outcome(outcome)
                .proposedFlowCommand(proposedFlowCommand)
                .proposedFlowId(proposedFlowId.get())
                .build();
    }
}
