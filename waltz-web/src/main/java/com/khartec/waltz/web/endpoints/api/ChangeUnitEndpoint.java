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
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.service.change_unit.ChangeUnitService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static java.lang.Long.parseLong;


@Service
public class ChangeUnitEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "change-unit");

    private final ChangeUnitService service;


    @Autowired
    public ChangeUnitEndpoint(ChangeUnitService service) {
        checkNotNull(service, "service cannot be null");
        this.service = service;
    }


    @Override
    public void register() {
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findBySubjectRefPath = mkPath(BASE_URL, "subject", ":kind", ":id");
        String findByChangeSetIdPath = mkPath(BASE_URL, "change-set", ":id");

        DatumRoute<ChangeUnit> getByIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.getById(parseLong(id));
        };

        ListRoute<ChangeUnit> findBySubjectRefRoute = (request, response) -> {
            EntityReference entityReference = getEntityReference(request);
            return service.findBySubjectRef(entityReference);
        };

        ListRoute<ChangeUnit> findByChangeSetIdRoute = (request, response) -> {
            String id = request.params("id");
            return service.findByChangeSetId(parseLong(id));
        };


        getForDatum(getByIdPath, getByIdRoute);
        getForList(findBySubjectRefPath, findBySubjectRefRoute);
        getForList(findByChangeSetIdPath, findByChangeSetIdRoute);
    }


}
