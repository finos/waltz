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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.EntityCost;
import com.khartec.waltz.service.cost.CostService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


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
        String findByCostKindAndSelectorPath = mkPath(BASE_URL, "cost-kind", ":id", "target-kind", ":kind");
        String findBySelectorPath = mkPath(BASE_URL, "target-kind", ":kind");

        ListRoute<EntityCost> findByEntityReferenceRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return costService.findByEntityReference(ref);
        };

        ListRoute<EntityCost> findByCostKindAndSelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            long costKindId = getId(request);
            Integer limit = getLimit(request).orElse(10);
            EntityKind targetKind = getKind(request);
            return costService.findByCostKindAndSelector(costKindId, idSelectionOptions, targetKind, limit);
        };

        ListRoute<EntityCost> findBySelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            EntityKind targetKind = getKind(request);
            return costService.findBySelector(idSelectionOptions, targetKind);
        };

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        postForList(findByCostKindAndSelectorPath, findByCostKindAndSelectorRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
    }

}
