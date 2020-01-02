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

import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.EntityRelationshipChangeCommand;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class ChangeInitiativeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "change-initiative");

    private final ChangeInitiativeService service;


    @Autowired
    public ChangeInitiativeEndpoint(ChangeInitiativeService service) {
        checkNotNull(service, "service cannot be null");

        this.service = service;
    }


    @Override
    public void register() {

        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String getRelationshipsForIdPath = mkPath(getByIdPath, "related");
        String findForSelectorPath = mkPath(BASE_URL, "selector");
        String findByExternalIdPath = mkPath(BASE_URL, "external-id", ":externalId");
        String findHierarchyForSelectorPath = mkPath(BASE_URL, "hierarchy", "selector");
        String searchPath = mkPath(BASE_URL, "search", ":query");
        String changeEntityRelationshipPath = mkPath(BASE_URL, "id", ":id", "entity-relationship");

        DatumRoute<ChangeInitiative> getByIdRoute = (request, response) ->
                service.getById(getId(request));

        ListRoute<EntityRelationship> getRelationshipsForIdRoute = (request, response) ->
                service.getRelatedEntitiesForId(getId(request));

        ListRoute<ChangeInitiative> findForSelectorRoute = (request, response) ->
                service.findForSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> findByExternalIdRoute = (request, response) ->
                service.findByExternalId(request.params("externalId"));

        ListRoute<ChangeInitiative> findHierarchyForSelectorRoute = (request, response) ->
                service.findHierarchyForSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> searchRoute = (request, response) ->
                service.search(request.params("query"));

        DatumRoute<Boolean> changeEntityRelationshipRoute = (request, response) -> changeEntityRelationship(request);


        getForDatum(getByIdPath, getByIdRoute);
        getForList(getRelationshipsForIdPath, getRelationshipsForIdRoute);
        getForList(searchPath, searchRoute);
        postForList(findForSelectorPath, findForSelectorRoute);
        getForList(findByExternalIdPath, findByExternalIdRoute);
        postForList(findHierarchyForSelectorPath, findHierarchyForSelectorRoute);
        postForDatum(changeEntityRelationshipPath, changeEntityRelationshipRoute);
    }


    private Boolean changeEntityRelationship(Request request) throws java.io.IOException {
        EntityRelationshipChangeCommand command = readBody(request, EntityRelationshipChangeCommand.class);
        switch (command.operation()) {
            case ADD:
                return service.addEntityRelationship(getId(request), command, getUsername(request));
            case REMOVE:
                return service.removeEntityRelationship(getId(request), command, getUsername(request));
            default:
                throw new UnsupportedOperationException("Command operation: "
                        + command.operation() + " is not supported");
        }
    }

}
