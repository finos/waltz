/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.allocation.AllocationService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class AllocationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "allocation");

    private final AllocationService allocationService;
    private final UserRoleService userRoleService;


    @Autowired
    public AllocationEndpoint(AllocationService allocationService,
                              UserRoleService userRoleService) {
        checkNotNull(allocationService, "allocationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");

        this.allocationService = allocationService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {
        String findByEntityPath = mkPath(BASE_URL,
                "entity-ref",
                ":kind",
                ":id");

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

        ListRoute<Allocation> findByEntityRoute = (request, response)
                -> allocationService.findByEntity(
                        getEntityReference(request));

        ListRoute<Allocation> findByEntityAndSchemeRoute = (request, response)
                -> allocationService.findByEntityAndScheme(
                        getEntityReference(request),
                        getLong(request,"scheme"));

        ListRoute<Allocation> findByMeasurableAndSchemeRoute = (request, response)
                -> allocationService.findByMeasurableAndScheme(
                        getLong(request, "measurable"),
                        getLong(request,"scheme"));

        DatumRoute<Boolean> updateAllocationsRoute = (request, response) -> {
            requireRole(userRoleService, request, SystemRole.RATING_EDITOR);
            MeasurablePercentageChange[] percentages = readBody(request, MeasurablePercentageChange[].class);
            return allocationService
                    .updateAllocations(
                        getEntityReference(request),
                        getLong(request,"scheme"),
                        newArrayList(percentages),
                        getUsername(request));
        };

        getForList(findByEntityPath, findByEntityRoute);
        getForList(findByEntityAndSchemePath, findByEntityAndSchemeRoute);
        getForList(findByMeasurableAndSchemePath, findByMeasurableAndSchemeRoute);
        postForDatum(updateAllocationsPath, updateAllocationsRoute);

    }

}
