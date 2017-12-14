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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.cost.ImmutableAssetCost;
import com.khartec.waltz.model.cost.ImmutableCost;
import com.khartec.waltz.schema.tables.records.AssetCostRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;


public class AssetCostGenerator {

    private static final Random rnd = new Random();

    private static final int year = 2016;
    private static final String provenance = "waltz";


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AssetCostRecord> appDevCosts = generateRecords(applicationService, "APPLICATION_DEVELOPMENT", 100_0000);
        List<AssetCostRecord> infraCosts = generateRecords(applicationService, "INFRASTRUCTURE", 5_000);

        dsl.deleteFrom(ASSET_COST)
                .where(ASSET_COST.YEAR.eq(year))
                .and(ASSET_COST.PROVENANCE.eq(provenance))
                .execute();

        dsl.batchInsert(appDevCosts).execute();
        dsl.batchInsert(infraCosts).execute();
    }


    private static List<AssetCostRecord> generateRecords(ApplicationService applicationService, String kind, int mean) {
        return applicationService.findAll()
                    .stream()
                    .filter(a -> a.assetCode().isPresent())
                    .map(a -> ImmutableAssetCost.builder()
                            .assetCode(a.assetCode().get())
                            .cost(ImmutableCost.builder()
                                    .amount(generateAmount(mean))
                                    .year(year)
                                    .costKind(kind)
                                    .build())
                            .build())
                    .map(c -> {
                        AssetCostRecord record = new AssetCostRecord();
                        record.setAssetCode(c.assetCode());
                        record.setAmount(c.cost().amount());
                        record.setKind(c.cost().costKind());
                        record.setYear(c.cost().year());
                        return record;
                    })
                    .collect(Collectors.toList());
    }


    private static BigDecimal generateAmount(long mean) {
        double z = mean / 3.4;
        double val = rnd.nextGaussian() * z + mean;

        return BigDecimal
                .valueOf(val)
                .setScale(2, RoundingMode.CEILING);
    }
}
