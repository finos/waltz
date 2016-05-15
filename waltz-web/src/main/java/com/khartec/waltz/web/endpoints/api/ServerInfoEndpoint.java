/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.serverinfo.ServerInfo;
import com.khartec.waltz.model.serverinfo.ServerSummaryStatistics;
import com.khartec.waltz.service.server_info.ServerInfoService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForDatum;


@Service
public class ServerInfoEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "server-info");

    private final ServerInfoService serverInfoService;


    @Autowired
    public ServerInfoEndpoint(ServerInfoService serverInfoService) {
        checkNotNull(serverInfoService, "serverInfoService must not be null");

        this.serverInfoService = serverInfoService;
    }


    @Override
    public void register() {

        String findByAssetCodePath = mkPath(BASE_URL, "asset-code", ":assetCode");
        String findByAppIdPath = mkPath(BASE_URL, "app-id", ":id");
        String findStatsForAppSelectorPath = mkPath(BASE_URL, "apps", "stats");

        ListRoute<ServerInfo> findByAssetCodeRoute = (request, response)
                -> serverInfoService.findByAssetCode(request.params("assetCode"));

        ListRoute<ServerInfo> findByAppIdRoute = (request, response)
                -> serverInfoService.findByAppId(getId(request));

        DatumRoute<ServerSummaryStatistics> findStatsForAppSelectorRoute = (request, response)
                -> serverInfoService.findStatsForAppSelector(readOptions(request));

        getForList(findByAssetCodePath, findByAssetCodeRoute);
        getForList(findByAppIdPath, findByAppIdRoute);
        postForDatum(findStatsForAppSelectorPath, findStatsForAppSelectorRoute);
    }


    private ApplicationIdSelectionOptions readOptions(Request request) throws java.io.IOException {
        return readBody(request, ApplicationIdSelectionOptions.class);
    }

}
