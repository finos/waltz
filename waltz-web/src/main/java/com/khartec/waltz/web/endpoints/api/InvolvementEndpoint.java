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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.involvement.EntityInvolvementChangeCommand;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

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
        String findDirectAppsByEmployeePath = mkPath(findByEmployeePath, "applications", "direct");
        String findAllAppsByEmployeePath = mkPath(findByEmployeePath, "applications");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findPeopleByEntityRefPath = mkPath(findByEntityRefPath, "people");

        String findBySelectorPath = mkPath(BASE_URL, "selector", "involvement");
        String findPeopleBySelectorPath = mkPath(BASE_URL, "selector", "people");

        String updateForEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");

        ListRoute<Involvement> findByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findByEmployeeId(employeeId);
        };

        ListRoute<Application>  findDirectAppsByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findDirectApplicationsByEmployeeId(employeeId);
        };

        ListRoute<Application>  findAllAppsByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findAllApplicationsByEmployeeId(employeeId);
        };

        ListRoute<Involvement> findByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findByEntityReference(entityReference);
        };

        ListRoute<Involvement> findBySelectorRoute = (request, response) -> {
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return service.findByGenericEntitySelector(selectionOptions);
        };

        ListRoute<Person>  findPeopleByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findPeopleByEntityReference(entityReference);
        };

        ListRoute<Person>  findPeopleBySelectorRoute = (request, response) -> {
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return service.findPeopleByGenericEntitySelector(selectionOptions);
        };

        DatumRoute<Boolean> updateForEntityRefRoute = (request, response) -> updateEntityInvolvement(request);

        getForList(findByEmployeePath, findByEmployeeRoute);
        getForList(findDirectAppsByEmployeePath, findDirectAppsByEmployeeRoute);
        getForList(findAllAppsByEmployeePath, findAllAppsByEmployeeRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        getForList(findPeopleByEntityRefPath, findPeopleByEntityRefRoute);
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
                EntityKind.ENTITY_NAMED_NOTE);
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
