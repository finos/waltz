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

import org.finos.waltz.service.server_information.ServerInformationService;
import org.finos.waltz.web.DatumRoute;
import org.finos.waltz.web.ListRoute;
import org.finos.waltz.web.endpoints.Endpoint;
import org.finos.waltz.model.server_information.ServerInformation;
import org.finos.waltz.model.server_information.ServerSummaryBasicStatistics;
import org.finos.waltz.model.server_information.ServerSummaryStatistics;
import org.finos.waltz.web.WebUtilities;
import org.finos.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;


@Service
public class ServerInformationEndpoint implements Endpoint {

    private static final String BASE_URL = WebUtilities.mkPath("api", "server-info");

    private final ServerInformationService serverInformationService;


    @Autowired
    public ServerInformationEndpoint(ServerInformationService serverInfoService) {
        checkNotNull(serverInfoService, "serverInformationService must not be null");

        this.serverInformationService = serverInfoService;
    }


    @Override
    public void register() {

        String findByAssetCodePath = WebUtilities.mkPath(BASE_URL, "asset-code", ":assetCode");
        String findByAppIdPath = WebUtilities.mkPath(BASE_URL, "app-id", ":id");
        String getByIdPath = WebUtilities.mkPath(BASE_URL, ":id");
        String getByExternalIdPath = WebUtilities.mkPath(BASE_URL, "external-id", ":externalId");
        String getByHostnamePath = WebUtilities.mkPath(BASE_URL, "hostname", ":hostname");
        String calculateStatsForAppSelectorPath = WebUtilities.mkPath(BASE_URL, "apps", "stats");
        String calculateBasicStatsForAppSelectorPath = WebUtilities.mkPath(calculateStatsForAppSelectorPath, "basic");

        ListRoute<ServerInformation> findByAssetCodeRoute = (request, response)
                -> serverInformationService.findByAssetCode(request.params("assetCode"));

        ListRoute<ServerInformation> findByAppIdRoute = (request, response)
                -> serverInformationService.findByAppId(WebUtilities.getId(request));

        DatumRoute<ServerInformation> getByIdRoute = (request, response)
                -> serverInformationService.getById(WebUtilities.getId(request));

        DatumRoute<ServerInformation> getByExternalIdRoute = (request, response)
                -> serverInformationService.getByExternalId(request.params("externalId"));

        DatumRoute<ServerInformation> getByHostnameRoute = (request, response)
                -> serverInformationService.getByHostname(request.params("hostname"));

        DatumRoute<ServerSummaryStatistics> calculateStatsForAppSelectorRoute = (request, response)
                -> serverInformationService.calculateStatsForAppSelector(WebUtilities.readIdSelectionOptionsFromBody(request));

        DatumRoute<ServerSummaryBasicStatistics> calculateBasicStatsForAppSelectorRoute = (request, response)
                -> serverInformationService.calculateBasicStatsForAppSelector(WebUtilities.readIdSelectionOptionsFromBody(request));


        EndpointUtilities.getForList(findByAssetCodePath, findByAssetCodeRoute);
        EndpointUtilities.getForList(findByAppIdPath, findByAppIdRoute);
        EndpointUtilities.getForDatum(getByIdPath, getByIdRoute);
        EndpointUtilities.getForDatum(getByExternalIdPath, getByExternalIdRoute);
        EndpointUtilities.getForDatum(getByHostnamePath, getByHostnameRoute);
        EndpointUtilities.postForDatum(calculateStatsForAppSelectorPath, calculateStatsForAppSelectorRoute);
        EndpointUtilities.postForDatum(calculateBasicStatsForAppSelectorPath, calculateBasicStatsForAppSelectorRoute);
    }


}
