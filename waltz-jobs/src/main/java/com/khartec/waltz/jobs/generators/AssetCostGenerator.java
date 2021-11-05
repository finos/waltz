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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.schema.tables.records.CostRecord;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.finos.waltz.common.RandomUtilities.randomPickSome;
import static com.khartec.waltz.jobs.WaltzUtilities.getOrCreateCostKind;
import static com.khartec.waltz.schema.Tables.COST;


public class AssetCostGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static final int year = LocalDate.now().getYear();



    private static List<CostRecord> generateRecords(List<Application> apps,
                                                    long costKind,
                                                    int mean) {
        return apps
                    .stream()
                    .map(a -> {
                        CostRecord record = new CostRecord();
                        record.setEntityId(a.id().get());
                        record.setEntityKind(EntityKind.APPLICATION.name());
                        record.setAmount(generateAmount(mean));
                        record.setCostKindId(costKind);
                        record.setYear(year);
                        record.setProvenance(SAMPLE_DATA_PROVENANCE);
                        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                        record.setLastUpdatedBy("admin");
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

        dsl.transaction(c -> {
            DSLContext tx = c.dsl();

            List<Application> apps = applicationService.findAll();

            List<CostRecord> appDevCosts = generateRecords(
                    apps,
                    getOrCreateCostKind(tx, "Application Development", "APPLICATION_DEVELOPMENT"),
                    900_000);

            List<CostRecord> infraCosts = generateRecords(
                    apps,
                    getOrCreateCostKind(tx, "Infrastructure", "INFRASTRUCTURE"),
                    50_000);

            List<CostRecord> cloudMigrationCosts = generateRecords(
                    randomPickSome(apps, 0.4),
                    getOrCreateCostKind(tx, "Cloud Migration", "CLOUD"),
                    200_000);

            List<CostRecord> depreciationCosts = generateRecords(
                    randomPickSome(apps, 0.6),
                    getOrCreateCostKind(tx, "Depreciation", "DEPRECIATION"),
                    100_000);

            tx.batchInsert(appDevCosts).execute();
            tx.batchInsert(infraCosts).execute();
            tx.batchInsert(cloudMigrationCosts).execute();
            tx.batchInsert(depreciationCosts).execute();
        });


        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(COST)
                .where(COST.YEAR.eq(year))
                .and(COST.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }
}
