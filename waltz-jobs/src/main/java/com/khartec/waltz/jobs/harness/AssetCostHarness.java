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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class AssetCostHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AssetCostService service = ctx.getBean(AssetCostService.class);
        AssetCostStatsDao statsDao = ctx.getBean(AssetCostStatsDao.class);
        AssetCostDao costDao = ctx.getBean(AssetCostDao.class);
        ApplicationIdSelectorFactory selectorFactory = new ApplicationIdSelectorFactory();


        long st = System.currentTimeMillis();
        System.out.println("-- start");

        IdSelectionOptions options = mkOpts(
                ImmutableEntityReference.builder()
                        .id(5600)
                        .kind(EntityKind.ORG_UNIT)
                        .build(),
                HierarchyQueryScope.CHILDREN);


        List<Tuple2<Long, BigDecimal>> costs = service.calculateCombinedAmountsForSelector(options);
        System.out.println("-- end, dur: " + (System.currentTimeMillis() - st));


        System.out.println(costs);
        System.out.println(costs.size());
    }




}
