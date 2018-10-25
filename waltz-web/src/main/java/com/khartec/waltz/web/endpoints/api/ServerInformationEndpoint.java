/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


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
        String calculateStatsForAppSelectorPath = mkPath(BASE_URL, "apps", "stats");

        ListRoute<ServerInformation> findByAssetCodeRoute = (request, response)
                -> serverInformationService.findByAssetCode(request.params("assetCode"));

        ListRoute<ServerInformation> findByAppIdRoute = (request, response)
                -> serverInformationService.findByAppId(getId(request));

        DatumRoute<ServerSummaryStatistics> calculateStatsForAppSelectorRoute = (request, response)
                -> serverInformationService.calculateStatsForAppSelector(readIdSelectionOptionsFromBody(request));

        getForList(findByAssetCodePath, findByAssetCodeRoute);
        getForList(findByAppIdPath, findByAppIdRoute);
        postForDatum(calculateStatsForAppSelectorPath, calculateStatsForAppSelectorRoute);
    }


}
