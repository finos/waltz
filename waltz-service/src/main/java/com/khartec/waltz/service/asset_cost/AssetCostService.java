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

package com.khartec.waltz.service.asset_cost;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.cost.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssetCostService {

    private final AssetCostDao assetCostDao;
    private final AssetCostStatsDao assetCostStatsDao;


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao, AssetCostStatsDao assetCostStatsDao) {
        this.assetCostDao = assetCodeDao;
        this.assetCostStatsDao = assetCostStatsDao;
    }


    public List<AssetCost> findByAssetCode(String code) {
        Checks.checkNotNull(code, "code cannot be null");
        return assetCostDao.findByAssetCode(code);
    }


    public List<AssetCost> findByAppId(long appId) {
        return assetCostDao.findByAppId(appId);
    }


    public List<ApplicationCost> findAppCostsByAppIds(AssetCostQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        return assetCostDao.findAppCostsByAppIds(options);
    }


    public AssetCostStatistics calculateStatisticsByAppIds(AssetCostQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");
        List<CostBandTally> costBandCounts = assetCostStatsDao.calculateCostBandStatisticsByAppIds(options);
        Cost totalCost = assetCostStatsDao.calculateTotalCostByAppIds(options);

        return ImmutableAssetCostStatistics.builder()
                .costBandCounts(costBandCounts)
                .totalCost(totalCost)
                .build();

    }

}
