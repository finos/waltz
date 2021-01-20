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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.EntityCostKind;
import com.khartec.waltz.service.cost_kind.CostKindService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class CostKindEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "cost-kind");

    private final CostKindService costKindService;


    @Autowired
    public CostKindEndpoint(CostKindService costKindService) {
        this.costKindService = costKindService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL);
        String findExistingBySelectorPath = mkPath(BASE_URL, "target-kind", ":kind", "selector");

        ListRoute<EntityCostKind> findAllRoute = (request, response) -> costKindService.findAll();

        ListRoute<EntityCostKind> findExistingBySelectorRoute = (request, response) -> {
            EntityKind targetKind = getKind(request);
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return costKindService.findExistingCostIdsBySelectorRoute(targetKind, selectionOptions);
        };

        getForList(findAllPath, findAllRoute);
        postForList(findExistingBySelectorPath, findExistingBySelectorRoute);
    }

}
