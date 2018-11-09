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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
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
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

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
        String findAllEndUserAppsBySelectorPath = mkPath(BASE_URL, "end-user-application");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findPeopleByEntityRefPath = mkPath(findByEntityRefPath, "people");
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

        ListRoute<EndUserApplication> findAllEndUserAppsBySelectorRoute = (request, response) -> {
            return service.findAllEndUserApplicationsBySelector(readEntityIdOptionsFromBody(request));
        };

        ListRoute<Involvement>  findByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findByEntityReference(entityReference);
        };

        ListRoute<Person>  findPeopleByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findPeopleByEntityReference(entityReference);
        };

        DatumRoute<Boolean> updateForEntityRefRoute = (request, response) -> updateEntityInvolvement(request);

        getForList(findByEmployeePath, findByEmployeeRoute);
        getForList(findDirectAppsByEmployeePath, findDirectAppsByEmployeeRoute);
        getForList(findAllAppsByEmployeePath, findAllAppsByEmployeeRoute);
        postForList(findAllEndUserAppsBySelectorPath, findAllEndUserAppsBySelectorRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findPeopleByEntityRefPath, findPeopleByEntityRefRoute);
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
