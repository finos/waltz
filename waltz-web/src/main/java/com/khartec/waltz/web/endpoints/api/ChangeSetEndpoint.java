/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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


import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.change_set.ChangeSet;
import com.khartec.waltz.service.change_set.ChangeSetService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;
import static java.lang.Long.parseLong;


@Service
public class ChangeSetEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "change-set");

    private final ChangeSetService changeSetService;


    @Autowired
    public ChangeSetEndpoint(ChangeSetService changeSetService) {
        checkNotNull(changeSetService, "changeSetService cannot be null");
        this.changeSetService = changeSetService;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByParentRefPath = mkPath(BASE_URL, "parent", ":kind", ":id");
        String findByPersonPath = mkPath(BASE_URL, "person", ":employeeId");
        String findBySelectorPath = mkPath(BASE_URL, "selector");


        DatumRoute<ChangeSet> getByIdRoute = (req, res) -> {
            String id = req.params("id");
            return changeSetService
                    .getById(parseLong(id));
        };

        ListRoute<ChangeSet> findByEntityRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return changeSetService.findByParentRef(entityReference);
        };

        ListRoute<ChangeSet> findBySelectorRoute = (request, response) ->
                changeSetService.findBySelector(readIdSelectionOptionsFromBody(request));

        ListRoute<ChangeSet> findByPersonRoute = (request, response) -> {
            String employeeId = request.params("employeeId");
            return changeSetService.findByPerson(employeeId);
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByPersonPath, findByPersonRoute);
        getForList(findByParentRefPath, findByEntityRefRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
    }



}
