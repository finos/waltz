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

import org.finos.waltz.service.change_initiative.ChangeInitiativeService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.entity_relationship.EntityRelationshipChangeCommand;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class ChangeInitiativeEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "change-initiative");

    private final ChangeInitiativeService service;


    @Autowired
    public ChangeInitiativeEndpoint(ChangeInitiativeService service) {
        checkNotNull(service, "service cannot be null");

        this.service = service;
    }


    @Override
    public void register() {

        String getByIdPath = WebUtilities.mkPath(BASE_URL, "id", ":id");
        String getRelationshipsForIdPath = WebUtilities.mkPath(getByIdPath, "related");
        String findForSelectorPath = WebUtilities.mkPath(BASE_URL, "selector");
        String findByExternalIdPath = WebUtilities.mkPath(BASE_URL, "external-id", ":externalId");
        String findHierarchyForSelectorPath = WebUtilities.mkPath(BASE_URL, "hierarchy", "selector");
        String searchPath = WebUtilities.mkPath(BASE_URL, "search", ":query");
        String changeEntityRelationshipPath = WebUtilities.mkPath(BASE_URL, "id", ":id", "entity-relationship");
        String findAllPath = WebUtilities.mkPath(BASE_URL, "all");

        DatumRoute<ChangeInitiative> getByIdRoute = (request, response) ->
                service.getById(WebUtilities.getId(request));

        ListRoute<EntityRelationship> getRelationshipsForIdRoute = (request, response) ->
                service.getRelatedEntitiesForId(WebUtilities.getId(request));

        ListRoute<ChangeInitiative> findForSelectorRoute = (request, response) ->
                service.findForSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> findByExternalIdRoute = (request, response) ->
                service.findByExternalId(request.params("externalId"));

        ListRoute<ChangeInitiative> findHierarchyForSelectorRoute = (request, response) ->
                service.findHierarchyForSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> searchRoute = (request, response) ->
                service.search(request.params("query"));

        ListRoute<ChangeInitiative> findAllRoute = (req, res) -> service.findAll();

        DatumRoute<Boolean> changeEntityRelationshipRoute = (request, response) -> changeEntityRelationship(request);


        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForList(getRelationshipsForIdPath, getRelationshipsForIdRoute);
        EndpointUtilities.getForList(searchPath, searchRoute);
        EndpointUtilities.postForList(findForSelectorPath, findForSelectorRoute);
        EndpointUtilities.getForList(findByExternalIdPath, findByExternalIdRoute);
        EndpointUtilities.getForList(findAllPath, findAllRoute);
        EndpointUtilities.postForList(findHierarchyForSelectorPath, findHierarchyForSelectorRoute);
        EndpointUtilities.postForDatum(changeEntityRelationshipPath, changeEntityRelationshipRoute);
    }


    private Boolean changeEntityRelationship(Request request) throws java.io.IOException {
        EntityRelationshipChangeCommand command = WebUtilities.readBody(request, EntityRelationshipChangeCommand.class);
        switch (command.operation()) {
            case ADD:
                return service.addEntityRelationship(WebUtilities.getId(request), command, WebUtilities.getUsername(request));
            case REMOVE:
                return service.removeEntityRelationship(WebUtilities.getId(request), command, WebUtilities.getUsername(request));
            default:
                throw new UnsupportedOperationException("Command operation: "
                        + command.operation() + " is not supported");
        }
    }

}
