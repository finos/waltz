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

package com.khartec.waltz.service.asset_cost;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.cost.ApplicationCost;
import com.khartec.waltz.model.cost.AssetCost;
import com.khartec.waltz.model.cost.Cost;
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
    private final ApplicationIdSelectorFactory idSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public AssetCostService(AssetCostDao assetCodeDao,
                            AssetCostStatsDao assetCostStatsDao) {
        checkNotNull(assetCodeDao, "assetCodeDao cannot be null");
        checkNotNull(assetCostStatsDao, "assetCostStatsDao cannot be null");

        this.assetCostDao = assetCodeDao;
        this.assetCostStatsDao = assetCostStatsDao;
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
