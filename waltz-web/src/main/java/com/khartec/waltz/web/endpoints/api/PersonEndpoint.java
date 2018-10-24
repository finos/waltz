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

import com.khartec.waltz.model.user.Role;
import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PersonEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "person");
    private static final String SEARCH_PATH = mkPath(BASE_URL, "search", ":query");
    private static final String DIRECTS_PATH = mkPath(BASE_URL, "employee-id", ":empId", "directs");
    private static final String COUNT_CUMULATIVE_REPORTS_BY_KIND_PATH = mkPath(BASE_URL, "employee-id", ":empId", "count-cumulative-reports");
    private static final String MANAGERS_PATH = mkPath(BASE_URL, "employee-id", ":empId", "managers");
    private static final String BY_EMPLOYEE_PATH = mkPath(BASE_URL, "employee-id", ":empId");
    private static final String GET_BY_USERID_PATH = mkPath(BASE_URL, "user-id", ":userId");
    private static final String GET_BY_ID = mkPath(BASE_URL, "id", ":id");
    private static final String REBUILD_HIERARCHY_PATH = mkPath(BASE_URL, "rebuild-hierarchy");

    private final PersonService personService;
    private final PersonHierarchyService personHierarchyService;
    private final UserRoleService userRoleService;


    @Autowired
    public PersonEndpoint(PersonService service,
                          PersonHierarchyService personHierarchyService,
                          UserRoleService userRoleService) {
        checkNotNull(service, "personService must not be null");
        this.personService = service;
        this.personHierarchyService = personHierarchyService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {

        getForList(SEARCH_PATH, (request, response) ->
                personService.search(request.params("query")));

        getForList(DIRECTS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return personService.findDirectsByEmployeeId(empId);
        });

        getForDatum(MANAGERS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return personService.findAllManagersByEmployeeId(empId);
        });

        getForDatum(BY_EMPLOYEE_PATH, (request, response) -> {
            String empId = request.params("empId");
            return personService.getByEmployeeId(empId);
        });

        getForDatum(GET_BY_ID, (request, response) ->
                personService.getById(getId(request)));

        getForDatum(GET_BY_USERID_PATH, ((request, response) ->
                personService.getPersonByUserId(request.params("userId"))));

        getForDatum(REBUILD_HIERARCHY_PATH, this::rebuildHierarchyRoute);

        getForDatum(COUNT_CUMULATIVE_REPORTS_BY_KIND_PATH, (req, res) ->
                personService.countAllUnderlingsByKind(req.params("empId")));

    }

    private boolean rebuildHierarchyRoute(Request request, Response response) {
        requireRole(userRoleService, request, Role.ADMIN);
        personHierarchyService.build();
        return true;
    }
}
