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

import com.khartec.waltz.service.server_info.ServerInfoService;
import com.khartec.waltz.web.endpoints.Endpoint;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.EndpointUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.requireRole;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForDatum;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


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

        getForList(mkPath(BASE_URL, "asset-code", ":assetCode"), (request, response)
                -> serverInfoService.findByAssetCode(request.params("assetCode")));

        getForList(mkPath(BASE_URL, "app-id", ":id"), (request, response)
                -> serverInfoService.findByAppId(getId(request)));

        getForDatum(mkPath(BASE_URL, "org-unit", ":id", "stats"), ((request, response)
                -> serverInfoService.findStatsForOrganisationalUnit(getId(request))));

    }
}
