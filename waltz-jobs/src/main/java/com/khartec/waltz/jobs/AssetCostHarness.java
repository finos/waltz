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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.asset_cost.AssetCostStatsDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import com.khartec.waltz.model.cost.AssetCostQueryOptions;
import com.khartec.waltz.model.cost.AssetCostStatistics;
import com.khartec.waltz.model.cost.CostBandTally;
import com.khartec.waltz.model.cost.ImmutableAssetCostQueryOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.asset_cost.AssetCostService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class AssetCostHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AssetCostService service = ctx.getBean(AssetCostService.class);
        AssetCostStatsDao dao = ctx.getBean(AssetCostStatsDao.class);
        ApplicationIdSelectorFactory selectorFactory = ctx.getBean(ApplicationIdSelectorFactory.class);


        long st = System.currentTimeMillis();
        System.out.println("-- start");

        ApplicationIdSelectionOptions appIdSelectionOptions = ImmutableApplicationIdSelectionOptions.builder()
                .scope(HierarchyQueryScope.EXACT)
                .entityReference(ImmutableEntityReference.builder()
                        .id(5000)
                        .kind(EntityKind.CAPABILITY)
                        .build())
                .build();

        AssetCostQueryOptions options = ImmutableAssetCostQueryOptions.builder()
                .idSelectionOptions(appIdSelectionOptions)
                .year(2015)
                .build();

        Select<Record1<Long>> selector = selectorFactory.apply(appIdSelectionOptions);
        List<CostBandTally> res = dao.calculateCostBandStatisticsByAppIdSelector(2015, selector);


        AssetCostStatistics stats = service.calculateStatisticsByAppIds(options);

        System.out.println("-- end, dur: " + (System.currentTimeMillis() - st));


        System.out.println(stats);
    }




}
