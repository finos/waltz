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

import com.khartec.waltz.service.complexity.ComplexityService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.complexity.Complexity;
import org.finos.waltz.model.complexity.ComplexitySummary;
import org.finos.waltz.model.complexity.ComplexityTotal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class ComplexityEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "complexity");

    private final ComplexityService complexityService;


    @Autowired
    public ComplexityEndpoint(ComplexityService complexityService) {
        this.complexityService = complexityService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = mkPath(BASE_URL, "entity", "kind", ":kind", "id", ":id");
        String findTotalsByTargetKindAndSelectorPath = mkPath(BASE_URL, "target-kind", ":kind", "totals");
        String findBySelectorPath = mkPath(BASE_URL, "target-kind", ":kind");
        String getComplexitySummaryForSelectorPath = mkPath(BASE_URL, "complexity-kind", ":id", "target-kind", ":kind");

        ListRoute<Complexity> findByEntityRefRoute = (request, response) -> complexityService
                .findByEntityReference(getEntityReference(request));

        ListRoute<ComplexityTotal> findTotalsByTargetKindAndSelectorRoute = (request, response) -> complexityService
                .findTotalsByTargetKindAndSelector(getKind(request), readIdSelectionOptionsFromBody(request));

        ListRoute<Complexity> findBySelectorRoute = (request, response) -> complexityService
                .findBySelector(getKind(request), readIdSelectionOptionsFromBody(request));

        DatumRoute<ComplexitySummary> getComplexitySummaryForSelectorRoute = (request, response) -> {
            long costKindId = getId(request);
            EntityKind targetKind = getKind(request);
            IdSelectionOptions selectionOptions = readIdSelectionOptionsFromBody(request);
            Integer limit = getLimit(request).orElse(15);

            return complexityService
                    .getComplexitySummaryForSelector(costKindId, targetKind, selectionOptions, limit);
        };

        getForList(findByEntityRefPath, findByEntityRefRoute);
        postForList(findTotalsByTargetKindAndSelectorPath, findTotalsByTargetKindAndSelectorRoute);
        postForList(findBySelectorPath, findBySelectorRoute);
        postForDatum(getComplexitySummaryForSelectorPath, getComplexitySummaryForSelectorRoute);
    }
}
