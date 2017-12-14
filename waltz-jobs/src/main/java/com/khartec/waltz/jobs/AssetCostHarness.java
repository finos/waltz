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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;


public class AssetCostHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AssetCostService service = ctx.getBean(AssetCostService.class);
        AssetCostStatsDao statsDao = ctx.getBean(AssetCostStatsDao.class);
        AssetCostDao costDao = ctx.getBean(AssetCostDao.class);
        ApplicationIdSelectorFactory selectorFactory = ctx.getBean(ApplicationIdSelectorFactory.class);


        long st = System.currentTimeMillis();
        System.out.println("-- start");

        IdSelectionOptions appIdSelectionOptions = ImmutableIdSelectionOptions.builder()
                .scope(HierarchyQueryScope.CHILDREN)
                .entityReference(ImmutableEntityReference.builder()
                        .id(5600)
                        .kind(EntityKind.ORG_UNIT)
                        .build())
                .build();


        List<Tuple2<Long, BigDecimal>> costs = service.calculateCombinedAmountsForSelector(appIdSelectionOptions);
        System.out.println("-- end, dur: " + (System.currentTimeMillis() - st));


        System.out.println(costs);
        System.out.println(costs.size());
    }




}
