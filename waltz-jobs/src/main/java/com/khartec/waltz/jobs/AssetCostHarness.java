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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.asset_cost.AssetCostDao;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.cost.AssetCostQueryOptions;
import com.khartec.waltz.model.cost.CostBandTally;
import com.khartec.waltz.model.cost.ImmutableAssetCostQueryOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;


public class AssetCostHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        AssetCostDao assetCostDao = ctx.getBean(AssetCostDao.class);
        AssetCostStatsDao assetCostStatsDao = ctx.getBean(AssetCostStatsDao.class);

        List<Long> appIds = newArrayList(
                801L,
                802L,
                803L);

        long st = System.currentTimeMillis();
        System.out.println("-- start");

        AssetCostQueryOptions options = ImmutableAssetCostQueryOptions.builder()
                .applicationIds(appIds)
                .year(2015)
                .build();

        List<CostBandTally> bands = assetCostStatsDao.calculateCostBandStatisticsByAppIds(options);

        System.out.println("-- end, dur: " + (System.currentTimeMillis() - st));


        bands.forEach(System.out::println);
    }




}
