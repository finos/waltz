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
import com.khartec.waltz.model.rating.RatingScheme;
import com.khartec.waltz.model.rating.RatingSchemeItem;
import com.khartec.waltz.model.user.SystemRole;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import com.khartec.waltz.service.user.UserRoleService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class RatingSchemeEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "rating-scheme");

    private final RatingSchemeService ratingSchemeService;
    private final UserRoleService userRoleService;


    @Autowired
    public RatingSchemeEndpoint(RatingSchemeService ratingSchemeService,
                                UserRoleService userRoleService) {
        checkNotNull(ratingSchemeService, "ratingSchemeService cannot be null");
        this.ratingSchemeService = ratingSchemeService;
        this.userRoleService = userRoleService;
    }


    @Override
    public void register() {
        String findAllPath = BASE_URL;
        String saveSchemePath = BASE_URL;
        String saveRatingItemPath = mkPath(BASE_URL, "id", ":id", "rating-item");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findRatingSchemeItemsForEntityAndCategoryPath = mkPath(BASE_URL, "items", "kind", ":kind", "id", ":id", "category-id", ":categoryId");
        String findRatingSchemeItemsPath = mkPath(BASE_URL, "items", "assessment-definition-id", ":id");

        ListRoute<RatingSchemeItem> findRatingSchemeItemsForEntityAndCategoryRoute = (request, response) -> {
            EntityReference ref = getEntityReference(request);
            long categoryId = getLong(request, "categoryId");

            return ratingSchemeService.findRatingSchemeItemsForEntityAndCategory(ref, categoryId);
        };

        getForList(findAllPath, (req, resp) -> ratingSchemeService.findAll());
        getForList(findRatingSchemeItemsForEntityAndCategoryPath, findRatingSchemeItemsForEntityAndCategoryRoute);
        getForList(findRatingSchemeItemsPath, (req, resp) -> ratingSchemeService.findRatingSchemeItemsByAssessmentDefinition(getId(req)));
        getForDatum(getByIdPath, (req, resp) -> ratingSchemeService.getById(getId(req)));
        putForDatum(saveSchemePath, this::saveScheme);
        putForDatum(saveRatingItemPath, this::saveRatingItem);
    }


    private Boolean saveRatingItem(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);
        long schemeId = getId(request);
        return ratingSchemeService.saveRatingItem(
                schemeId,
                readBody(request, RatingSchemeItem.class));
    }


    private Boolean saveScheme(Request request, Response response) throws IOException {
        ensureUserHasEditRights(request);
        return ratingSchemeService.save(readBody(request, RatingScheme.class));
    }

    private void ensureUserHasEditRights(Request request) {
        requireRole(userRoleService, request, SystemRole.ADMIN);
    }

}
