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

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.cost.Cost;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


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
        String findByAppIdPath = mkPath(BASE_URL, "app-cost", ":id");
        String findAppCostsByAppIdsPath = mkPath(BASE_URL, "app-cost", "apps");
        String findTopAppCostsByAppIdsPath = mkPath(BASE_URL, "app-cost", "top-apps");
        String calculateTotalCostForAppSelectorPath = mkPath(BASE_URL, "app-cost", "apps", "total");
        String calcCombinedAmountsForSelectorPath = mkPath(BASE_URL, "amount", "app-selector");

        getForList(findByAssetCodePath, this::findByAssetCodeRoute);
        getForList(findByAppIdPath, this::findByAppIdRoute);
        postForList(findAppCostsByAppIdsPath, this::findAppCostsByAppIds);
        postForList(findTopAppCostsByAppIdsPath, this::findTopAppCostsByAppIds);
        postForDatum(calculateTotalCostForAppSelectorPath, this::calculateTotalCostForAppSelectorRoute);
        postForList(calcCombinedAmountsForSelectorPath, this::calcCombinedAmountsForSelectorRoute);

    }


    private List<AssetCost> findByAssetCodeRoute(Request request, Response response) {
        return assetCostService.findByAssetCode(request.params("code"));
    }


    private List<AssetCost> findByAppIdRoute(Request request, Response response) {
        return assetCostService.findByAppId(getId(request));
    }


    private List<Tuple2<Long, BigDecimal>> calcCombinedAmountsForSelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.calculateCombinedAmountsForSelector(selectorOptions);
    }


    private Cost calculateTotalCostForAppSelectorRoute(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.calculateTotalCostForAppSelector(selectorOptions);
    }


    private List<ApplicationCost> findAppCostsByAppIds(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.findAppCostsByAppIds(selectorOptions);
    }


    private List<ApplicationCost> findTopAppCostsByAppIds(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.findTopAppCostsByAppIds(selectorOptions, getLimit(request).orElse(10));
    }

}
