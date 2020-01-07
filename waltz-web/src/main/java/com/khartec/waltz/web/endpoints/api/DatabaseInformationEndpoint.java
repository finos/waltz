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

import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import com.khartec.waltz.service.database_information.DatabaseInformationService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.json.ApplicationDatabases;
import com.khartec.waltz.web.json.ImmutableApplicationDatabases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;

@Service
public class DatabaseInformationEndpoint implements Endpoint {

    private final DatabaseInformationService databaseInformationService;

    private static final String BASE_URL = mkPath("api", "database");

    @Autowired
    public DatabaseInformationEndpoint(DatabaseInformationService databaseInformationService) {
        this.databaseInformationService = databaseInformationService;
    }

    @Override
    public void register() {

        String findForAppPath = mkPath(BASE_URL, "app", ":id");
        String findForAppSelectorPath = mkPath(BASE_URL);
        String calculateStatsForAppIdSelectorPath = mkPath(BASE_URL, "stats");


        ListRoute<DatabaseInformation> findForAppRoute = (request, response)
                -> databaseInformationService.findByApplicationId(getId(request));

        ListRoute<ApplicationDatabases> findForAppSelectorRoute = (request, response)
                -> databaseInformationService.findByApplicationSelector(readIdSelectionOptionsFromBody(request))
                    .entrySet()
                    .stream()
                    .map(e -> ImmutableApplicationDatabases.builder()
                            .applicationId(e.getKey())
                            .databases(e.getValue())
                            .build())
                    .collect(Collectors.toList());

        DatumRoute<DatabaseSummaryStatistics> calculateStatsForAppIdSelectorRoute = (request, response)
                -> databaseInformationService.calculateStatsForAppIdSelector(readIdSelectionOptionsFromBody(request));


        getForList(findForAppPath, findForAppRoute);
        postForList(findForAppSelectorPath, findForAppSelectorRoute);
        postForDatum(calculateStatsForAppIdSelectorPath, calculateStatsForAppIdSelectorRoute);

    }
}
