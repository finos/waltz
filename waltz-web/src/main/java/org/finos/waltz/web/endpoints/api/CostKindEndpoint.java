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

package org.finos.waltz.web.endpoints.api;

import org.finos.waltz.model.cost.CostKindWithYears;
import org.finos.waltz.service.cost_kind.CostKindService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.getForList;
import static org.finos.waltz.web.endpoints.EndpointUtilities.postForList;


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
        String findCostKindsBySelectorPath = mkPath(BASE_URL, "target-kind", ":kind", "selector");

        ListRoute<CostKindWithYears> findAllRoute = (request, response) -> costKindService.findAll();

        ListRoute<CostKindWithYears> findCostKindsBySelectorRoute = (request, response) -> {
            EntityKind targetKind = getKind(request);
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            return costKindService.findCostKindsSelectorRoute(targetKind, selectionOptions);
        };

        getForList(findAllPath, findAllRoute);
        postForList(findCostKindsBySelectorPath, findCostKindsBySelectorRoute);
    }

}
