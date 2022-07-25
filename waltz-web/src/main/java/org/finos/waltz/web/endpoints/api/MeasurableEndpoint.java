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

import org.finos.waltz.service.measurable.MeasurableService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.measurable.Measurable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.web.WebUtilities.*;
import static org.finos.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class MeasurableEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "measurable");

    private final MeasurableService measurableService;


    @Autowired
    public MeasurableEndpoint(MeasurableService measurableService) {
        this.measurableService = measurableService;
    }


    @Override
    public void register() {

        String findAllPath = mkPath(BASE_URL, "all");
        String getByIdPath = mkPath(BASE_URL, "id", ":id");
        String findByMeasurableIdSelectorPath = mkPath(BASE_URL, "measurable-selector");
        String findByExternalIdPath = mkPath(BASE_URL, "external-id", ":extId");
        String findByOrgUnitIdPath = mkPath(BASE_URL, "org-unit", "id", ":id");
        String searchPath = mkPath(BASE_URL, "search", ":query");

        ListRoute<Measurable> findAllRoute = (request, response)
                -> measurableService.findAll();

        DatumRoute<Measurable> getByIdRoute = (request, response)
                -> measurableService.getById(getId(request));

        ListRoute<Measurable> findByMeasurableIdSelectorRoute = (request, response)
                -> measurableService.findByMeasurableIdSelector(readIdSelectionOptionsFromBody(request));

        ListRoute<Measurable> searchRoute = (request, response)
                -> measurableService.search(request.params("query"));

        ListRoute<Measurable> findByExternalIdRoute = (request, response)
                -> measurableService.findByExternalId(request.params("extId"));

        ListRoute<Measurable> findByOrgUnitIdRoute = (request, response)
                -> measurableService.findByOrgUnitId(getId(request));


        getForList(findAllPath, findAllRoute);
        getForList(findByExternalIdPath, findByExternalIdRoute);
        postForList(findByMeasurableIdSelectorPath, findByMeasurableIdSelectorRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForList(searchPath, searchRoute);
        getForList(findByOrgUnitIdPath, findByOrgUnitIdRoute);
    }

}
