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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.cost.ImmutableAssetCost;
import com.khartec.waltz.model.cost.ImmutableAssetCost;
import com.khartec.waltz.model.cost.ImmutableCost;
import com.khartec.waltz.schema.tables.records.AssetCostRecord;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.AssetCost.ASSET_COST;


public class AssetCostGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static final int year = 2017;



    private static List<AssetCostRecord> generateRecords(ApplicationService applicationService, String costKind, int mean) {
        return applicationService.findAll()
                    .stream()
                    .filter(a -> a.assetCode().isPresent())
                    .map(a -> ImmutableAssetCost.builder()
                            .assetCode(a.assetCode().get())
                            .cost(ImmutableCost.builder()
                                    .amount(generateAmount(mean))
                                    .year(year)
                                    .costKind(costKind)
                                    .build())
                            .build())
                    .map(c -> {
                        AssetCostRecord record = new AssetCostRecord();
                        record.setAssetCode(c.assetCode());
                        record.setAmount(c.cost().amount());
                        record.setKind(c.cost().costKind());
                        record.setYear(c.cost().year());
                        record.setProvenance(SAMPLE_DATA_PROVENANCE);
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

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AssetCostRecord> appDevCosts = generateRecords(applicationService, "APPLICATION_DEVELOPMENT", 70_0000);
        List<AssetCostRecord> infraCosts = generateRecords(applicationService, "INFRASTRUCTURE", 5_000);


        dsl.batchInsert(appDevCosts).execute();
        dsl.batchInsert(infraCosts).execute();
        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(ASSET_COST)
                .where(ASSET_COST.YEAR.eq(year))
                .and(ASSET_COST.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }
}
