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

import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class AssetCostEndpoint implements Endpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BookmarksEndpoint.class);
    private static final String BASE_URL = mkPath("api", "asset-cost");

    private final AssetCostService assetCostService;


    @Autowired
    public AssetCostEndpoint(AssetCostService assetCostService) {
        this.assetCostService = assetCostService;
    }


    @Override
    public void register() {

        String findByAssetCodePath = mkPath(BASE_URL, "code", ":code");
        String findByOrgUnitPath = mkPath(BASE_URL, "org-unit-tree", ":id");
        String findAppCostsByOrgUnitPath = mkPath(BASE_URL, "app-cost", "org-unit-tree", ":id");

        ListRoute<AssetCost> findByAssetCodeRoute = (request, response) -> assetCostService.findByAssetCode(request.params("code"));
        ListRoute<AssetCost> findByOrgUnitRoute = (request, response) -> assetCostService.findByOrgUnitTree(getId(request));
        ListRoute<ApplicationCost> findAppCostsByOrgUnitRoute = (request, response) -> assetCostService.findAppCostsByOrgUnitTree(getId(request));

        getForList(findByAssetCodePath, findByAssetCodeRoute);
        getForList(findByOrgUnitPath, findByOrgUnitRoute);
        getForList(findAppCostsByOrgUnitPath, findAppCostsByOrgUnitRoute);
    }
}
