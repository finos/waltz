/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.web.endpoints.api;


import org.finos.waltz.service.entity_workflow.EntityWorkflowService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.entity_workflow.EntityWorkflowDefinition;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntityWorkflowEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "entity-workflow");


    private final EntityWorkflowService entityWorkflowService;


    @Autowired
    public EntityWorkflowEndpoint(EntityWorkflowService entityWorkflowService) {
        this.entityWorkflowService = entityWorkflowService;
    }


    @Override
    public void register() {
        String findAllDefinitionsPath = WebUtilities.mkPath(BASE_URL, "definition");
        String getStateForEntityAndWorkflowPath = WebUtilities.mkPath("entity-ref", ":kind", ":id", ":workflowId", "state");
        String findTransitionsForEntityAndWorkflowPath = WebUtilities.mkPath("entity-ref", ":kind", ":id", ":workflowId", "transition");

        ListRoute<EntityWorkflowDefinition> findAllDefinitionsRoute = (request, response)
                -> entityWorkflowService.findAllDefinitions();

        DatumRoute<EntityWorkflowState> getStateForEntityAndWorkflowRoute = (request, response)
                -> entityWorkflowService.getStateForEntityReferenceAndWorkflowId(
                        WebUtilities.getLong(request, "workflowId"),
                        WebUtilities.getEntityReference(request));


        ListRoute<EntityWorkflowTransition> findTransitionsForEntityAndWorkflowRoute = (request, response)
                -> entityWorkflowService.findTransitionsForEntityReferenceAndWorkflowId(
                        WebUtilities.getLong(request, "workflowId"),
                        WebUtilities.getEntityReference(request));


        EndpointUtilities.getForList(findAllDefinitionsPath, findAllDefinitionsRoute);
        EndpointUtilities.getForDatum(getStateForEntityAndWorkflowPath, getStateForEntityAndWorkflowRoute);
        EndpointUtilities.getForList(findTransitionsForEntityAndWorkflowPath, findTransitionsForEntityAndWorkflowRoute);
    }
}
