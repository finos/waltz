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

import org.finos.waltz.service.cost.CostService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.cost.EntityCost;
import org.finos.waltz.model.cost.EntityCostsSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


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
        String findBySelectorPath = mkPath(BASE_URL, "target-kind", ":kind");
        String summariseByCostKindAndSelectorPath = mkPath(BASE_URL, "cost-kind", ":id", "target-kind", ":kind", "summary", ":year");

        ListRoute<EntityCost> findByEntityReferenceRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            return costService.findByEntityReference(ref);
        };

        DatumRoute<EntityCostsSummary> summariseByCostKindAndSelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            long costKindId = getId(request);
            int year = getInt(request, "year");
            Integer limit = getLimit(request).orElse(15);
            EntityKind targetKind = getKind(request);
            return costService.summariseByCostKindAndSelector(costKindId, idSelectionOptions, targetKind, year, limit);
        };

        ListRoute<EntityCost> findBySelectorRoute = (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            EntityKind targetKind = getKind(request);
            return costService.findBySelector(idSelectionOptions, targetKind);
        };

        getForList(findByEntityReferencePath, findByEntityReferenceRoute);
        postForDatum(summariseByCostKindAndSelectorPath, summariseByCostKindAndSelectorRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
    }

}
