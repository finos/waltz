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
