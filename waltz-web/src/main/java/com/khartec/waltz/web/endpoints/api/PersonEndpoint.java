/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class PersonEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "person");
    private static final String SEARCH_PATH = mkPath(BASE_URL, "search", ":query");
    private static final String DIRECTS_PATH = mkPath(BASE_URL, "employee-id", ":empId", "directs");
    private static final String MANAGERS_PATH = mkPath(BASE_URL, "employee-id", ":empId", "managers");
    private static final String BY_EMPLOYEE_PATH = mkPath(BASE_URL, "employee-id", ":empId");
    private static final String FIND_BY_USERID_PATH = mkPath(BASE_URL, "user-id", ":userId");

    private final PersonService service;


    @Autowired
    public PersonEndpoint(PersonService service) {
        checkNotNull(service, "service must not be null");
        this.service = service;
    }


    @Override
    public void register() {

        getForList(SEARCH_PATH, (request, response) ->
                service.search(request.params("query")));

        getForList(DIRECTS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.findDirectsByEmployeeId(empId);
        });

        getForDatum(MANAGERS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.findAllManagersByEmployeeId(empId);
        });

        getForDatum(BY_EMPLOYEE_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.getByEmployeeId(empId);
        });

        getForDatum(FIND_BY_USERID_PATH, ((request, response) ->
                service.findPersonByUserId(request.params("userId"))));

    }
}
