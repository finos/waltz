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

import org.finos.waltz.service.complexity.ComplexityService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.complexity.Complexity;
import org.finos.waltz.model.complexity.ComplexitySummary;
import org.finos.waltz.model.complexity.ComplexityTotal;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ComplexityEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "complexity");

    private final ComplexityService complexityService;


    @Autowired
    public ComplexityEndpoint(ComplexityService complexityService) {
        this.complexityService = complexityService;
    }


    @Override
    public void register() {
        String findByEntityRefPath = WebUtilities.mkPath(BASE_URL, "entity", "kind", ":kind", "id", ":id");
        String findTotalsByTargetKindAndSelectorPath = WebUtilities.mkPath(BASE_URL, "target-kind", ":kind", "totals");
        String findBySelectorPath = WebUtilities.mkPath(BASE_URL, "target-kind", ":kind");
        String getComplexitySummaryForSelectorPath = WebUtilities.mkPath(BASE_URL, "complexity-kind", ":id", "target-kind", ":kind");

        ListRoute<Complexity> findByEntityRefRoute = (request, response) -> complexityService
                .findByEntityReference(WebUtilities.getEntityReference(request));

        ListRoute<ComplexityTotal> findTotalsByTargetKindAndSelectorRoute = (request, response) -> complexityService
                .findTotalsByTargetKindAndSelector(WebUtilities.getKind(request), WebUtilities.readIdSelectionOptionsFromBody(request));

        ListRoute<Complexity> findBySelectorRoute = (request, response) -> complexityService
                .findBySelector(WebUtilities.getKind(request), WebUtilities.readIdSelectionOptionsFromBody(request));

        DatumRoute<ComplexitySummary> getComplexitySummaryForSelectorRoute = (request, response) -> {
            long costKindId = WebUtilities.getId(request);
            EntityKind targetKind = WebUtilities.getKind(request);
            IdSelectionOptions selectionOptions = WebUtilities.readIdSelectionOptionsFromBody(request);
            Integer limit = WebUtilities.getLimit(request).orElse(15);

            return complexityService
                    .getComplexitySummaryForSelector(costKindId, targetKind, selectionOptions, limit);
        };

        EndpointUtilities.getForList(findByEntityRefPath, findByEntityRefRoute);
        EndpointUtilities.postForList(findTotalsByTargetKindAndSelectorPath, findTotalsByTargetKindAndSelectorRoute);
        EndpointUtilities.postForList(findBySelectorPath, findBySelectorRoute);
        EndpointUtilities.postForDatum(getComplexitySummaryForSelectorPath, getComplexitySummaryForSelectorRoute);
    }
}
