package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.entity_workflow.EntityWorkflowDefinition;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowState;
import com.khartec.waltz.model.entity_workflow.EntityWorkflowTransition;
import com.khartec.waltz.service.entity_workflow.EntityWorkflowService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class EntityWorkflowEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "entity-workflow");


    private final EntityWorkflowService entityWorkflowService;


    @Autowired
    public EntityWorkflowEndpoint(EntityWorkflowService entityWorkflowService) {
        this.entityWorkflowService = entityWorkflowService;
    }


    @Override
    public void register() {
        String findAllDefinitionsPath = mkPath(BASE_URL, "definition");
        String getStateForEntityAndWorkflowPath = mkPath("entity-ref", ":kind", ":id", ":workflowId", "state");
        String findTransitionsForEntityAndWorkflowPath = mkPath("entity-ref", ":kind", ":id", ":workflowId", "transition");

        ListRoute<EntityWorkflowDefinition> findAllDefinitionsRoute = (request, response)
                -> entityWorkflowService.findAllDefinitions();

        DatumRoute<EntityWorkflowState> getStateForEntityAndWorkflowRoute = (request, response)
                -> entityWorkflowService.getStateForEntityReferenceAndWorkflowId(
                        getLong(request, "workflowId"),
                        getEntityReference(request));


        ListRoute<EntityWorkflowTransition> findTransitionsForEntityAndWorkflowRoute = (request, response)
                -> entityWorkflowService.findTransitionsForEntityReferenceAndWorkflowId(
                        getLong(request, "workflowId"),
                        getEntityReference(request));


        getForList(findAllDefinitionsPath, findAllDefinitionsRoute);
        getForDatum(getStateForEntityAndWorkflowPath, getStateForEntityAndWorkflowRoute);
        getForList(findTransitionsForEntityAndWorkflowPath, findTransitionsForEntityAndWorkflowRoute);
    }
}
