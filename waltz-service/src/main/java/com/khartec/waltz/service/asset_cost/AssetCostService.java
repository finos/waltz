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
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.cost.*;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssetCostService {

    private final AssetCostDao assetCostDao;
    private final AssetCostStatsDao assetCostStatsDao;
    private final ApplicationIdSelectorFactory idSelectorFactory;


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao,
                            AssetCostStatsDao assetCostStatsDao,
                            ApplicationIdSelectorFactory idSelectorFactory) {
        Checks.checkNotNull(assetCodeDao, "assetCodeDao cannot be null");
        Checks.checkNotNull(assetCostStatsDao, "assetCostStatsDao cannot be null");
        Checks.checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");

        this.assetCostDao = assetCodeDao;
        this.assetCostStatsDao = assetCostStatsDao;
        this.idSelectorFactory = idSelectorFactory;
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
        Select<Record1<Long>> selector = idSelectorFactory.apply(options.idSelectionOptions());
        return assetCostDao.findAppCostsByAppIdSelector(options.year(), selector);
    }


    public AssetCostStatistics calculateStatisticsByAppIds(AssetCostQueryOptions options) {
        Checks.checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options.idSelectionOptions());

        List<CostBandTally> costBandCounts = assetCostStatsDao.calculateCostBandStatisticsByAppIdSelector(options.year(), appIdSelector);
        Cost totalCost = assetCostStatsDao.calculateTotalCostByAppIdSelector(options.year(), appIdSelector);

        return ImmutableAssetCostStatistics.builder()
                .costBandCounts(costBandCounts)
                .totalCost(totalCost)
                .build();

    }

}
