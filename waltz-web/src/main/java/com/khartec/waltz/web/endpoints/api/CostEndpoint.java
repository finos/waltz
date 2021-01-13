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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.cost.EntityCost;
import com.khartec.waltz.service.cost.CostService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class CostEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "cost");

    private final CostService costService;


    @Autowired
    public CostEndpoint(CostService costService) {
        this.costService = costService;
    }


    @Override
    public void register() {

        String findByEntityReferencePath = mkPath(BASE_URL, "entity", "kind", ":kind", "id", ":id");

        ListRoute<EntityCost> findByEntityReferenceRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return costService.findByEntityReference(ref);
        };

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
    }

}
