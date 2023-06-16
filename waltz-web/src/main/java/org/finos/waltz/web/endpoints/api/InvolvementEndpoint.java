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

import org.finos.waltz.common.Checks;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.involvement.EntityInvolvementChangeCommand;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.service.involvement.InvolvementService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class InvolvementEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "involvement");

    private final InvolvementService service;
    private final UserRoleService userRoleService;


    @Autowired
    public InvolvementEndpoint(InvolvementService service,
                               UserRoleService userRoleService) {
        this.service = service;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        String findByEmployeePath = mkPath(BASE_URL, "employee", ":employeeId");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findPeopleByEntityRefPath = mkPath(findByEntityRefPath, "people");
        String findExistingInvolvementKindIdsForUserPath = mkPath(findByEntityRefPath, "user");

        String findBySelectorPath = mkPath(BASE_URL, "selector", "involvement");
        String findPeopleBySelectorPath = mkPath(BASE_URL, "selector", "people");
        String countOrphanInvolvementsForKindPath = mkPath(BASE_URL, "entity-kind", ":kind", "orphan-count");
        String cleanupInvalidInvolvementsForEntityPath = mkPath(BASE_URL, "entity-kind", ":kind", "cleanup-orphans");

        String updateForEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");

        ListRoute<Involvement> findByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findByEmployeeId(employeeId);
        };

        ListRoute<Involvement> findByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findByEntityReference(entityReference);
        };

        ListRoute<Long> findExistingInvolvementKindIdsForUserRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findExistingInvolvementKindIdsForUser(entityReference, getUsername(request));
        };

        ListRoute<Involvement> findBySelectorRoute = (request, response) -> {
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return service.findByGenericEntitySelector(selectionOptions);
        };

        ListRoute<Person> findPeopleByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findPeopleByEntityReference(entityReference);
        };

        ListRoute<Person> findPeopleBySelectorRoute = (request, response) -> {
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return service.findPeopleByGenericEntitySelector(selectionOptions);
        };

        DatumRoute<Integer> countOrphanInvolvementsForKindRoute = (request, response) -> {
            EntityKind kind = getKind(request);
            return service.countOrphanInvolvementsForKind(kind);
        };

        DatumRoute<Integer> cleanupInvalidInvolvementsForKindRoute = (request, response) -> {
            EntityKind kind = getKind(request);
            return service.cleanupInvolvementsForKind(getUsername(request), kind);
        };

        DatumRoute<Boolean> updateForEntityRefRoute = (request, response) -> updateEntityInvolvement(request);

        getForList(findByEmployeePath, findByEmployeeRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForDatum(countOrphanInvolvementsForKindPath, countOrphanInvolvementsForKindRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        getForList(findPeopleByEntityRefPath, findPeopleByEntityRefRoute);
        getForList(findExistingInvolvementKindIdsForUserPath, findExistingInvolvementKindIdsForUserRoute);
        deleteForDatum(cleanupInvalidInvolvementsForEntityPath, cleanupInvalidInvolvementsForKindRoute);
        postForList(findPeopleBySelectorPath, findPeopleBySelectorRoute);
        postForDatum(updateForEntityRefPath, updateForEntityRefRoute);
    }


    private Boolean updateEntityInvolvement(Request request) throws java.io.IOException {
        EntityReference entityReference = getEntityReference(request);
        EntityInvolvementChangeCommand command = readBody(request, EntityInvolvementChangeCommand.class);

        requireEditRoleForEntity(
                userRoleService,
                request,
                entityReference.kind(),
                command.operation(),
                EntityKind.INVOLVEMENT);

        String username = getUsername(request);

        switch (command.operation()) {
            case ADD:
                return service.addEntityInvolvement(username, entityReference, command);
            case REMOVE:
                return service.removeEntityInvolvement(username, entityReference, command);
            default:
                throw new UnsupportedOperationException("Command operation: "
                        + command.operation() + " is not supported");
        }
    }

}
