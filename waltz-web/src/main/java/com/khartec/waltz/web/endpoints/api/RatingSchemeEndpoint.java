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
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class RatingSchemeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "rating-scheme");
    private final RatingSchemeService ratingSchemeService;

    @Autowired
    public RatingSchemeEndpoint(RatingSchemeService ratingSchemeService) {
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        this.ratingSchemeService = ratingSchemeService;
    }

    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findRatingSchemeItemsForEntityAndCategoryPath = mkPath(BASE_URL, "items", "kind", ":kind", "id", ":id", "category-id", ":categoryId");

        ListRoute<RagName> findRatingSchemeItemsForEntityAndCategoryRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            long categoryId = getLong(request, "categoryId");

            return ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(ref, categoryId);
        };

        getForList(findAllPath, (req, resp) -> ratingSchemeService.findAll());
        getForList(findRatingSchemeItemsForEntityAndCategoryPath, findRatingSchemeItemsForEntityAndCategoryRoute);
        getForDatum(getByIdPath, (req, resp) -> ratingSchemeService.getById(getId(req)));
    }
}
