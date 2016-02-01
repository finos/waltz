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

import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.postForList;


@Service
public class AssetCostEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "asset-cost");

    private final AssetCostService assetCostService;


    @Autowired
    public AssetCostEndpoint(AssetCostService assetCostService) {
        this.assetCostService = assetCostService;
    }


    @Override
    public void register() {

        String findByAssetCodePath = mkPath(BASE_URL, "code", ":code");
        String findAppCostsByAppIdsPath = mkPath(BASE_URL, "app-cost", "apps");

        ListRoute<AssetCost> findByAssetCodeRoute = (request, response) -> assetCostService.findByAssetCode(request.params("code"));
        ListRoute<ApplicationCost> findAppCostsByAppIds = (request, response) -> assetCostService.findAppCostsByAppIds(readBody(request, Long[].class));

        getForList(findByAssetCodePath, findByAssetCodeRoute);
        postForList(findAppCostsByAppIdsPath, findAppCostsByAppIds);

    }
}
