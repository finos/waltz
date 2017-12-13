/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        String findHierarchyForSelectorPath = mkPath(BASE_URL, "hierarchy", "selector");
        String searchPath = mkPath(BASE_URL, "search", ":query");
        String changeEntityRelationshipPath = mkPath(BASE_URL, "id", ":id", "entity-relationship");

        DatumRoute<ChangeInitiative> getByIdRoute = (request, response) ->
                service.getById(getId(request));

        ListRoute<EntityRelationship> getRelationshipsForIdRoute = (request, response) ->
                service.getRelatedEntitiesForId(getId(request));

        ListRoute<ChangeInitiative> findForSelectorRoute = (request, response) ->
                service.findForSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> findHierarchyForSelectorRoute = (request, response) ->
                service.findHierarchyForSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeInitiative> searchRoute = (request, response) ->
                service.search(request.params("query"));

        DatumRoute<Boolean> changeEntityRelationshipRoute = (request, response) -> changeEntityRelationship(request);


        getForDatum(getByIdPath, getByIdRoute);
        getForList(getRelationshipsForIdPath, getRelationshipsForIdRoute);
        getForList(searchPath, searchRoute);
        postForList(findForSelectorPath, findForSelectorRoute);
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
