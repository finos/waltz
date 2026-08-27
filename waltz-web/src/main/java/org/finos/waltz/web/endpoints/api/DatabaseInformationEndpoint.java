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

import org.finos.waltz.service.database_information.DatabaseInformationService;
import org.finos.waltz.service.user.UserRoleService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.web.json.ApplicationDatabases;
import org.finos.waltz.web.json.ImmutableApplicationDatabases;
import org.finos.waltz.model.database_information.DatabaseInformation;
import org.finos.waltz.model.database_information.DatabaseSummaryStatistics;
import org.finos.waltz.model.user.SystemRole;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class DatabaseInformationEndpoint implements Endpoint {

    private final DatabaseInformationService databaseInformationService;
    private final UserRoleService userRoleService;

    private static final String BASE_URL = WebUtilities.mkPath("api", "database");

    @Autowired
    public DatabaseInformationEndpoint(DatabaseInformationService databaseInformationService,
                                       UserRoleService userRoleService) {
        checkNotNull(databaseInformationService, "databaseInformationService cannot be null");
        checkNotNull(userRoleService, "userRoleService cannot be null");
        this.databaseInformationService = databaseInformationService;
        this.userRoleService = userRoleService;
    }

    @Override
    public void register() {

        String findForAppPath = WebUtilities.mkPath(BASE_URL, "app", ":id");
        String getByIdPath = WebUtilities.mkPath(BASE_URL, ":id");
        String getByExternalIdPath = WebUtilities.mkPath(BASE_URL, "external-id", ":externalId");
        String findForAppSelectorPath = WebUtilities.mkPath(BASE_URL);
        String calculateStatsForAppIdSelectorPath = WebUtilities.mkPath(BASE_URL, "stats");
        String bulkSavePath = WebUtilities.mkPath(BASE_URL, "bulk");


        ListRoute<DatabaseInformation> findForAppRoute = (request, response)
                -> databaseInformationService.findByApplicationId(WebUtilities.getId(request));

        DatumRoute<DatabaseInformation> getByIdRoute = (request, response)
                -> databaseInformationService.getById(WebUtilities.getId(request));

        DatumRoute<DatabaseInformation> getByExternalIdRoute = (request, response)
                -> databaseInformationService.getByExternalId(request.params("externalId"));

        ListRoute<ApplicationDatabases> findForAppSelectorRoute = (request, response)
                -> databaseInformationService.findByApplicationSelector(WebUtilities.readIdSelectionOptionsFromBody(request))
                    .entrySet()
                    .stream()
                    .map(e -> ImmutableApplicationDatabases.builder()
                            .applicationId(e.getKey())
                            .databases(e.getValue())
                            .build())
                    .collect(Collectors.toList());

        DatumRoute<DatabaseSummaryStatistics> calculateStatsForAppIdSelectorRoute = (request, response)
                -> databaseInformationService.calculateStatsForAppIdSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        DatumRoute<Collection<DatabaseInformation>> bulkSaveRoute = (request, response) -> {
            WebUtilities.requireRole(userRoleService, request, SystemRole.ADMIN);
            String username = WebUtilities.getUsername(request);
            List<DatabaseInformation> databases = WebUtilities.readList(request, DatabaseInformation.class);
            return databaseInformationService.bulkSave(databases, username);
        };


        EndpointUtilities.getForList(findForAppPath, findForAppRoute);
        EndpointUtilities.postForList(findForAppSelectorPath, findForAppSelectorRoute);
        EndpointUtilities.postForDatum(calculateStatsForAppIdSelectorPath, calculateStatsForAppIdSelectorRoute);
        EndpointUtilities.postForDatum(bulkSavePath, bulkSaveRoute);
        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForDatum(getByExternalIdPath, getByExternalIdRoute);

    }
}
