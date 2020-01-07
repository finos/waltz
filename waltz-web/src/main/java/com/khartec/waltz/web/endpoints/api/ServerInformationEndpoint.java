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

import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import com.khartec.waltz.service.server_information.ServerInformationService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class ServerInformationEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "server-info");

    private final ServerInformationService serverInformationService;


    @Autowired
    public ServerInformationEndpoint(ServerInformationService serverInfoService) {
        checkNotNull(serverInfoService, "serverInformationService must not be null");

        this.serverInformationService = serverInfoService;
    }


    @Override
    public void register() {

        String findByAssetCodePath = mkPath(BASE_URL, "asset-code", ":assetCode");
        String findByAppIdPath = mkPath(BASE_URL, "app-id", ":id");
        String getByIdPath = mkPath(BASE_URL, ":id");
        String getByExternalIdPath = mkPath(BASE_URL, "external-id", ":externalId");
        String getByHostnamePath = mkPath(BASE_URL, "hostname", ":hostname");
        String calculateStatsForAppSelectorPath = mkPath(BASE_URL, "apps", "stats");

        ListRoute<ServerInformation> findByAssetCodeRoute = (request, response)
                -> serverInformationService.findByAssetCode(request.params("assetCode"));

        ListRoute<ServerInformation> findByAppIdRoute = (request, response)
                -> serverInformationService.findByAppId(getId(request));

        DatumRoute<ServerInformation> getByIdRoute = (request, response)
                -> serverInformationService.getById(getId(request));

        DatumRoute<ServerInformation> getByExternalIdRoute = (request, response)
                -> serverInformationService.getByExternalId(request.params("externalId"));

        DatumRoute<ServerInformation> getByHostnameRoute = (request, response)
                -> serverInformationService.getByHostname(request.params("hostname"));

        DatumRoute<ServerSummaryStatistics> calculateStatsForAppSelectorRoute = (request, response)
                -> serverInformationService.calculateStatsForAppSelector(readIdSelectionOptionsFromBody(request));


        getForList(findByAssetCodePath, findByAssetCodeRoute);
        getForList(findByAppIdPath, findByAppIdRoute);
        getForDatum(getByIdPath, getByIdRoute);
        getForDatum(getByExternalIdPath, getByExternalIdRoute);
        getForDatum(getByHostnamePath, getByHostnameRoute);
        postForDatum(calculateStatsForAppSelectorPath, calculateStatsForAppSelectorRoute);
    }


}
