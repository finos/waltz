package com.khartec.waltz.web.endpoints.api;

import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.cost.AssetCostStatistics;
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
        String calcStatisticsByAppIdsPath = mkPath(BASE_URL, "app-cost", "apps", "stats");
        String calcCombinedAmountsForSelectorPath = mkPath(BASE_URL, "amount", "app-selector");

        getForList(findByAssetCodePath, this::findByAssetCodeRoute);
        getForList(findByAppIdPath, this::findByAppIdRoute);
        postForList(findAppCostsByAppIdsPath, this::findAppCostsByAppIds);
        postForDatum(calcStatisticsByAppIdsPath, this::calcStatisticsByAppIdsRoute);
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


    private AssetCostStatistics calcStatisticsByAppIdsRoute(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.calculateStatisticsByAppIds(selectorOptions);
    }


    private List<ApplicationCost> findAppCostsByAppIds(Request request, Response response) throws IOException {
        IdSelectionOptions selectorOptions = readIdSelectionOptionsFromBody(request);
        return assetCostService.findAppCostsByAppIds(selectorOptions);
    }

}
