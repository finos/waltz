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

import com.khartec.waltz.model.allocation_scheme.AllocationScheme;
import com.khartec.waltz.service.allocation_schemes.AllocationSchemeService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class AllocationSchemeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "allocation-scheme");

    private final AllocationSchemeService allocationSchemesService;


    @Autowired
    public AllocationSchemeEndpoint(AllocationSchemeService allocationSchemesService) {
        this.allocationSchemesService = allocationSchemesService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL, "all");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByCategoryPath = mkPath(BASE_URL, "category", ":id");

        ListRoute<AllocationScheme> findAllRoute = (request, response)
                -> allocationSchemesService.findAll();


        DatumRoute<AllocationScheme> getByIdRoute = (request, response)
                -> allocationSchemesService.getById(getId(request));


        ListRoute<AllocationScheme> findByCategoryRoute = (request, response)
                -> allocationSchemesService.findByCategoryId(getId(request));


        getForList(findAllPath, findAllRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForList(findByCategoryPath, findByCategoryRoute);
    }

}
