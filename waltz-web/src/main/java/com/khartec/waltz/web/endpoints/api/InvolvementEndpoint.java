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
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.involvement.InvolvementService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readEntityIdOptionsFromBody;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;

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

        String findByEmployeePath = mkPath(BASE_URL, "employee", ":employeeId");
        String findDirectAppsByEmployeePath = mkPath(findByEmployeePath, "applications", "direct");
        String findDirectChangeInitiativesByEmployeePath = mkPath(findByEmployeePath, "change-initiative", "direct");
        String findAllAppsByEmployeePath = mkPath(findByEmployeePath, "applications");
        String findAllEndUserAppsBySelectorPath = mkPath(BASE_URL, "end-user-application");
        String findByEntityRefPath = mkPath(BASE_URL, "entity", ":kind", ":id");
        String findPeopleByEntityRefPath = mkPath(findByEntityRefPath, "people");


        ListRoute<Involvement> findByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findByEmployeeId(employeeId);
        };

        ListRoute<Application>  findDirectAppsByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findDirectApplicationsByEmployeeId(employeeId);
        };

        ListRoute<ChangeInitiative>  findDirectChangeInitiativesByEmployeeRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return service.findDirectChangeInitiativesByEmployeeId(employeeId);
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


        getForList(findByEmployeePath, findByEmployeeRoute);
        getForList(findDirectAppsByEmployeePath, findDirectAppsByEmployeeRoute);
        getForList(findDirectChangeInitiativesByEmployeePath, findDirectChangeInitiativesByEmployeeRoute);
        getForList(findAllAppsByEmployeePath, findAllAppsByEmployeeRoute);
        postForList(findAllEndUserAppsBySelectorPath, findAllEndUserAppsBySelectorRoute);
        getForList(findByEntityRefPath, findByEntityRefRoute);
        getForList(findPeopleByEntityRefPath, findPeopleByEntityRefRoute);
    }
}
