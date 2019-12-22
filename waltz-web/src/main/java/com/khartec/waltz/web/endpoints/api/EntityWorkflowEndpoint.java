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
