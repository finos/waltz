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

import org.finos.waltz.service.rating_scheme.RatingSchemeService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rating.RatingScheme;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.user.SystemRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;
import static org.finos.waltz.common.Checks.checkNotNull;


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
        String removeRatingSchemePath = mkPath(BASE_URL, "id", ":id");

        String findRatingSchemeItemsForEntityAndCategoryPath = mkPath(BASE_URL, "items", "kind", ":kind", "id", ":id", "category-id", ":categoryId");
        String findRatingSchemeItemsPath = mkPath(BASE_URL, "items", "assessment-definition-id", ":id");
        String findAllRatingSchemeItemsPath = mkPath(BASE_URL, "items");
        String calcRatingUsageStatsPath = mkPath(BASE_URL, "items", "usage");
        String removeRatingItemPath = mkPath(BASE_URL, "items", "id", ":id");

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
        getForList(calcRatingUsageStatsPath, (req, resp) -> ratingSchemeService.calcRatingUsageStats());
        getForList(findAllRatingSchemeItemsPath, (req, resp) -> ratingSchemeService.findAllRatingSchemeItems());
        deleteForDatum(removeRatingItemPath, this::removeRatingItem);
        deleteForDatum(removeRatingSchemePath, this::removeRatingScheme);
    }


    private Boolean removeRatingScheme(Request request, Response response) {
        ensureUserHasEditRights(request);
        return ratingSchemeService.removeRatingScheme(getId(request));
    }


    private Boolean removeRatingItem(Request request, Response response) {
        ensureUserHasEditRights(request);
        return ratingSchemeService.removeRatingItem(getId(request));
    }


    private Long saveRatingItem(Request request, Response response) throws IOException {
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
        requireAnyRole(userRoleService, request, SystemRole.RATING_SCHEME_ADMIN, SystemRole.ADMIN);
    }

}
