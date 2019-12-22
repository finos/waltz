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
