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

package com.khartec.waltz.service.asset_cost;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.*;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AssetCostService {

    private final AssetCostDao assetCostDao;
    private final AssetCostStatsDao assetCostStatsDao;
    private final ApplicationIdSelectorFactory idSelectorFactory;


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao,
                            AssetCostStatsDao assetCostStatsDao,
                            ApplicationIdSelectorFactory idSelectorFactory) {
        checkNotNull(assetCodeDao, "assetCodeDao cannot be null");
        checkNotNull(assetCostStatsDao, "assetCostStatsDao cannot be null");
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");

        this.assetCostDao = assetCodeDao;
        this.assetCostStatsDao = assetCostStatsDao;
        this.idSelectorFactory = idSelectorFactory;
    }


    public List<AssetCost> findByAssetCode(String code) {
        checkNotNull(code, "code cannot be null");
        return assetCostDao.findByAssetCode(code);
    }


    public List<AssetCost> findByAppId(long appId) {
        return assetCostDao.findByAppId(appId);
    }


    public List<ApplicationCost> findAppCostsByAppIds(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);

        return assetCostDao
                .findLatestYear()
                .map(year -> assetCostDao.findAppCostsByAppIdSelector(year, selector))
                .orElse(Collections.emptyList());
    }


    public List<ApplicationCost> findTopAppCostsByAppIds(IdSelectionOptions options, int limit) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);

        return assetCostDao
                .findLatestYear()
                .map(year -> assetCostDao.findTopAppCostsByAppIdSelector(year, selector, limit))
                .orElse(Collections.emptyList());
    }


    public List<Tuple2<Long, BigDecimal>> calculateCombinedAmountsForSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);

        return assetCostDao
                .findLatestYear()
                .map(y -> assetCostDao.calculateCombinedAmountsForSelector(y, appIdSelector))
                .orElse(Collections.emptyList());
    }


    public Cost calculateTotalCostForAppSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");

        Select<Record1<Long>> appIdSelector = idSelectorFactory.apply(options);

        return assetCostDao
                .findLatestYear()
                .map(year -> assetCostStatsDao
                            .calculateTotalCostByAppIdSelector(year, appIdSelector))
                .orElse(null);
    }

}
