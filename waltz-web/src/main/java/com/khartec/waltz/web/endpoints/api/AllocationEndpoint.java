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


import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.MeasurablePercentage;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import com.khartec.waltz.service.allocation.AllocationService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class AllocationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "allocation");

    private final AllocationService allocationService;


    @Autowired
    public AllocationEndpoint(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    @Override
    public void register() {
        String findByEntityAndSchemePath = mkPath(BASE_URL,
                "entity-ref",
                ":kind",
                ":id",
                ":scheme");

        String findByMeasurableAndSchemePath = mkPath(BASE_URL,
                "measurable",
                ":measurable",
                ":scheme");

        String updateAllocationsPath = mkPath(BASE_URL,
                "entity-ref",
                ":kind",
                ":id",
                ":scheme",
                "allocations");

        ListRoute<Allocation> findByEntityAndSchemeRoute = (request, response)
                -> allocationService.findByEntityAndScheme(
                        getEntityReference(request),
                        getLong(request,"scheme"));

        ListRoute<Allocation> findByMeasurableAndSchemeRoute = (request, response)
                -> allocationService.findByMeasurableAndScheme(
                        getLong(request, "measurable"),
                        getLong(request,"scheme"));

        DatumRoute<Boolean> updateAllocationsRoute = (request, response) -> {
            MeasurablePercentageChange[] percentages = readBody(request, MeasurablePercentageChange[].class);
            return allocationService
                    .updateAllocations(
                        getEntityReference(request),
                        getLong(request,"scheme"),
                        newArrayList(percentages),
                        getUsername(request));
        };

        getForList(findByEntityAndSchemePath, findByEntityAndSchemeRoute);
        getForList(findByMeasurableAndSchemePath, findByMeasurableAndSchemeRoute);
        postForDatum(updateAllocationsPath, updateAllocationsRoute);

    }

}
