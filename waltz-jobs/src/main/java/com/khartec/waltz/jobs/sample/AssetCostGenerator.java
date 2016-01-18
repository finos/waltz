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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.cost.CostKind;
import com.khartec.waltz.model.cost.ImmutableAssetCost;
import com.khartec.waltz.model.cost.ImmutableCost;
import com.khartec.waltz.schema.tables.records.AssetCostRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;


public class AssetCostGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AssetCostRecord> appDevCosts = generateRecords(applicationService, CostKind.APPLICATION_DEVELOPMENT, 10_000, 10_000_000);
        List<AssetCostRecord> infraCosts = generateRecords(applicationService, CostKind.INFRASTRUCTURE, 1_000, 50_000);

        dsl.deleteFrom(ASSET_COST).execute();

        dsl.batchInsert(appDevCosts).execute();
        dsl.batchInsert(infraCosts).execute();
    }


    private static List<AssetCostRecord> generateRecords(ApplicationService applicationService, CostKind kind, int low, int high) {
        return applicationService.getAll()
                    .stream()
                    .filter(a -> a.assetCode().isPresent())
                    .map(a -> ImmutableAssetCost.builder()
                            .assetCode(a.assetCode().get())
                            .cost(ImmutableCost.builder()
                                    .currencyCode("EUR")
                                    .amount(generateAmount(low, high))
                                    .year(2015)
                                    .kind(kind)
                                    .build())
                            .build())
                    .map(c -> {
                        AssetCostRecord record = new AssetCostRecord();
                        record.setAssetCode(c.assetCode());
                        record.setAmount(c.cost().amount());
                        record.setCurrency(c.cost().currencyCode());
                        record.setKind(c.cost().kind().name());
                        record.setYear(c.cost().year());
                        return record;
                    })
                    .collect(Collectors.toList());
    }


    private static BigDecimal generateAmount(int low, int high) {
        int amount = rnd.nextInt(high - low) + low;
        return BigDecimal.valueOf(amount);
    }
}
