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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;

@Service
public class InvolvementEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "involvement");

    private final InvolvementService service;


    @Autowired
    public InvolvementEndpoint(InvolvementService service) {
        this.service = service;
    }


    @Override
    public void register() {

        ListRoute<Involvement> byEmployeeId = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findByEmployeeId(employeeId);
        };

        ListRoute<Application>  findDirectAppsByEmployeeId = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findDirectApplicationsByEmployeeId(employeeId);
        };

        ListRoute<Application>  findAllAppsByEmployeeId = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findAllApplicationsByEmployeeId(employeeId);
        };

        ListRoute<Involvement>  findByEntityRef = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findByEntityReference(entityReference);
        };

        ListRoute<Person>  findPeopleByEntityRef = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findPeopleByEntityReference(entityReference);
        };

        getForList(mkPath(BASE_URL, "employee", ":employeeId"), byEmployeeId);
        getForList(mkPath(BASE_URL, "employee", ":employeeId", "applications", "direct"), findDirectAppsByEmployeeId);
        getForList(mkPath(BASE_URL, "employee", ":employeeId", "applications"), findAllAppsByEmployeeId);
        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findByEntityRef);
        getForList(mkPath(BASE_URL, "entity", ":kind", ":id", "people"), findPeopleByEntityRef);
    }
}
