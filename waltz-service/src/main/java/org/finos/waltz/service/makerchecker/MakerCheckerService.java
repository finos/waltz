package org.finos.waltz.service.makerchecker;

import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.proposed_flow.ProposedFlowDao;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.jooq.exception.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MakerCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final ProposedFlowDao proposedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        ProposedFlowDao proposedFlowDao){
        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.proposedFlowDao = proposedFlowDao;
    }

    public List<EntityWorkflowTransition> proposedNewFlow(String requestBody, String username, ProposedFlowCommand proposedFlowCommand){
        Long proposedFlowId = proposedFlowDao.saveRequestedFlow(requestBody, username, proposedFlowCommand);
        LOG.info("New ProposedFlowId is : {} ", proposedFlowId);
        EntityWorkflowDefinition ewd = entityWorkflowService.searchByName("Requested Flow Lifecycle Workflow");
        if(ewd.id().isPresent()){
            entityWorkflowStateDao.saveNewWorkflowState(proposedFlowId, ewd.id().get(), username);
            entityWorkflowTransitionDao.saveNewWorkflowTransition(proposedFlowId, ewd.id().get(), username);
            List<EntityWorkflowTransition> list =  entityWorkflowTransitionDao.findForWorkflowId(ewd.id().get());
            return list;
        }else{
            throw new NoDataFoundException("Could not find workflow definition: Requested Flow Lifecycle Workflow");
        }

    }
}
