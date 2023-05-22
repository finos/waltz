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

import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategory;
import org.finos.waltz.service.measurable_category.MeasurableCategoryService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.finos.waltz.web.WebUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class MeasurableCategoryEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable-category");

    private final MeasurableCategoryService measurableCategoryService;


    @Autowired
    public MeasurableCategoryEndpoint(MeasurableCategoryService measurableCategoryService) {
        this.measurableCategoryService = measurableCategoryService;
    }


    @Override
    public void register() {
        String findAllPath = mkPath(BASE_URL, "all");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String getCategoriesByDirectOrgUnitPath = mkPath(BASE_URL, "direct", "org-unit", ":id");
        String savePath = mkPath(BASE_URL, "save");

        ListRoute<MeasurableCategory> findAllRoute = (request, response)
                -> measurableCategoryService.findAll();

        ListRoute<MeasurableCategory> findCategoriesByDirectOrgUnitRoute = (request, response)
                -> measurableCategoryService.findCategoriesByDirectOrgUnit(WebUtilities.getId(request));

        DatumRoute<MeasurableCategory> getByIdRoute = (request, response)
                -> measurableCategoryService.getById(WebUtilities.getId(request));

        DatumRoute<Boolean> saveRoute = (request, response) -> {
            String username = getUsername(request);
            return measurableCategoryService
                    .save(
                            ImmutableMeasurableCategory
                                    .copyOf(readBody(request, MeasurableCategory.class))
                                    .withLastUpdatedAt(nowUtc())
                                    .withLastUpdatedBy(username),
                            username);

        };

        getForList(findAllPath, findAllRoute);
        getForList(getCategoriesByDirectOrgUnitPath, findCategoriesByDirectOrgUnitRoute);
        getForDatum(getByIdPath, getByIdRoute);
        postForDatum(savePath, saveRoute);
    }

}
