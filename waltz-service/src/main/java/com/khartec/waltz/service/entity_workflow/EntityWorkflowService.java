package com.khartec.waltz.service.entity_workflow;


import com.khartec.waltz.data.entity_workflow.EntityWorkflowDefinitionDao;
import com.khartec.waltz.data.entity_workflow.EntityWorkflowStateDao;
import com.khartec.waltz.data.entity_workflow.EntityWorkflowTransitionDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowDefinition;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowState;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityWorkflowService {

    private final EntityWorkflowDefinitionDao entityWorkflowDefinitionDao;
    private final EntityWorkflowStateDao entityWorkflowStateDao;
    private final EntityWorkflowTransitionDao entityWorkflowTransitionDao;

    @Autowired
    public EntityWorkflowService(EntityWorkflowDefinitionDao entityWorkflowDefinitionDao,
                                 EntityWorkflowStateDao entityWorkflowStateDao,
                                 EntityWorkflowTransitionDao entityWorkflowTransitionDao) {
        this.entityWorkflowDefinitionDao = entityWorkflowDefinitionDao;
        this.entityWorkflowStateDao = entityWorkflowStateDao;
        this.entityWorkflowTransitionDao = entityWorkflowTransitionDao;
    }


    public List<EntityWorkflowDefinition> findAllDefinitions() {
        return entityWorkflowDefinitionDao.findAll();
    }


    public EntityWorkflowState getStateForEntityReferenceAndWorkflowId(long workflowId,
                                                                       EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowStateDao.getByEntityReferenceAndWorkflowId(workflowId, ref);
    }


    public List<EntityWorkflowTransition> findTransitionsForEntityReferenceAndWorkflowId(long workflowId,
                                                                                         EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return entityWorkflowTransitionDao.findForEntityReferenceAndWorkflowId(workflowId, ref);
    }
}
