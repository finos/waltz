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
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class PersonEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "person");
    private static final String SEARCH_PATH = WebUtilities.mkPath(BASE_URL, "search", ":query");
    private static final String DIRECTS_PATH = WebUtilities.mkPath(BASE_URL, "employee-id", ":empId", "directs");
    private static final String MANAGERS_PATH = WebUtilities.mkPath(BASE_URL, "employee-id", ":empId", "managers");
    private static final String BY_EMPLOYEE_PATH = WebUtilities.mkPath(BASE_URL, "employee-id", ":empId");

    private final PersonService service;


    @Autowired
    public PersonEndpoint(PersonService service) {
        checkNotNull(service, "service must not be null");
        this.service = service;
    }


    @Override
    public void register() {

        EndpointUtilities.getForList(SEARCH_PATH, (request, response) ->
                service.search(request.params("query")));

        EndpointUtilities.getForList(DIRECTS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.findDirectsByEmployeeId(empId);
        });

        EndpointUtilities.getForDatum(MANAGERS_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.findAllManagersByEmployeeId(empId);
        });

        EndpointUtilities.getForDatum(BY_EMPLOYEE_PATH, (request, response) -> {
            String empId = request.params("empId");
            return service.getByEmployeeId(empId);
        });

    }
}
