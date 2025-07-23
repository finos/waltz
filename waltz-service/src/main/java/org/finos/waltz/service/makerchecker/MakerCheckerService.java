package org.finos.waltz.service.makerchecker;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.entity_workflow.EntityWorkflowStateDao;
import org.finos.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import org.finos.waltz.data.requested_flow.RequestedFlowDao;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.model.requested_flow.RequestedFlowCommand;
import org.finos.waltz.schema.tables.records.RequestedFlowRecord;
import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.jooq.DSLContext;
import org.jooq.exception.NoDataFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import static org.finos.waltz.schema.tables.RequestedFlow.REQUESTED_FLOW;

@Service
public class MakerCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(MakerCheckerService.class);
    private final EntityWorkflowService entityWorkflowService;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final RequestedFlowDao requestedFlowDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Autowired
    MakerCheckerService(EntityWorkflowService entityWorkflowService,
                        EntityWorkflowStateDao entityWorkflowStateDao,
                        EntityWorkflowTransitionDao entityWorkflowTransitionDao,
                        RequestedFlowDao requestedFlowDao){
        this.entityWorkflowService = entityWorkflowService;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
        this.requestedFlowDao = requestedFlowDao;
    }

    public List<EntityWorkflowTransition> proposedNewFlow(String requestBody, String username, RequestedFlowCommand requestedFlowCommand){
        Long requestedFlowId = requestedFlowDao.saveRequestedFlow(requestBody, username, requestedFlowCommand);
        LOG.info("New RequestedFlowId is : {} ", requestedFlowId);
        EntityWorkflowDefinition ewd = entityWorkflowService.searchByName("Requested Flow Lifecycle Workflow");
        if(ewd.id().isPresent()){
            entityWorkflowStateDao.saveNewWorkflowState(requestedFlowId, ewd.id().get(), username);
            entityWorkflowTransitionDao.saveNewWorkflowTransition(requestedFlowId, ewd.id().get(), username);
            List<EntityWorkflowTransition> list =  entityWorkflowTransitionDao.findForWorkflowId(ewd.id().get());
            return list;
        }else{
            throw new NoDataFoundException("Could not find workflow definition: Requested Flow Lifecycle Workflow");
        }

    }
}
