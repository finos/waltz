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

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.software_catalog.SoftwareCatalog;
import com.khartec.waltz.model.software_catalog.SoftwareSummaryStatistics;
import com.khartec.waltz.service.software_catalog.SoftwareCatalogService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;

@Service
public class SoftwareCatalogEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "software-catalog");

    private final SoftwareCatalogService service;

    @Autowired
    public SoftwareCatalogEndpoint(SoftwareCatalogService service) {
        this.service = service;
    }


    @Override
    public void register() {

        String makeCatalogForAppIdsPath = mkPath(BASE_URL, "apps");
        String calculateStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        DatumRoute<SoftwareCatalog> makeCatalogForAppIdsRoute = (request, response) ->
                service.makeCatalogForAppIds(readIdsFromBody(request));

        DatumRoute<SoftwareSummaryStatistics> calculateStatsForAppIdSelectorRoute = (request, response)
                -> service.calculateStatisticsForAppIdSelector(readIdSelectionOptionsFromBody(request));


        getForDatum(mkPath(BASE_URL, "package-id", ":id"), this::getByPackageIdRoute);
        getForDatum(mkPath(BASE_URL, "version-id", ":id"), this::getByVersionIdRoute);
        getForDatum(mkPath(BASE_URL, "licence-id", ":id"), this::getByLicenceIdRoute);
        postForDatum(mkPath(BASE_URL, "selector"), this::findBySelectorRoute);
        postForDatum(makeCatalogForAppIdsPath, makeCatalogForAppIdsRoute);
        postForDatum(calculateStatsForAppIdSelectorPath, calculateStatsForAppIdSelectorRoute);

    }


    private SoftwareCatalog getByPackageIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getByPackageId(id);
    }


    private SoftwareCatalog getByVersionIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getByVersionId(id);
    }


    private SoftwareCatalog getByLicenceIdRoute(Request request, Response response) {
        long id = getId(request);
        return service.getByLicenceId(id);
    }


    private SoftwareCatalog findBySelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions options = readIdSelectionOptionsFromBody(request);
        return service.findBySelector(options);
    }

}
