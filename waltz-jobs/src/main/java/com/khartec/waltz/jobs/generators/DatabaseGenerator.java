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

import org.finos.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.schema.tables.records.DatabaseInformationRecord;
import com.khartec.waltz.schema.tables.records.DatabaseUsageRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;

public class DatabaseGenerator implements SampleDataGenerator {


    private final Random rnd = RandomUtilities.getRandom();


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<DatabaseInformationRecord> databaseRecords = new LinkedList<>();

        for (int i = 0; i < 150; i++) {
            int num = rnd.nextInt(10);

            for (int j = 0 ; j < num; j++) {
                SoftwarePackage pkg = randomPick(DatabaseSoftwarePackages.dbs);

                DatabaseInformationRecord databaseRecord = dsl.newRecord(DATABASE_INFORMATION);
                databaseRecord.setDatabaseName("DB_LON_" + i + "_" + j);
                databaseRecord.setInstanceName("DB_INST_" + i);
                databaseRecord.setDbmsVendor(pkg.vendor());
                databaseRecord.setDbmsName(pkg.name());
                databaseRecord.setDbmsVersion("1.0.1");
                databaseRecord.setExternalId("ext_" + i + "_" +j);
                databaseRecord.setProvenance("RANDOM_GENERATOR");
                databaseRecord.setLifecycleStatus(randomPick(LifecycleStatus.values()).toString());
                databaseRecord.setEndOfLifeDate(
                        rnd.nextInt(10) > 5
                                ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                                : null);

                databaseRecords.add(databaseRecord);
            }
        }

        dsl.batchStore(databaseRecords).execute();

        // create Database usages
        List<Long> appIds = getAppIds(dsl);
        List<Long> databaseIds = getDatabaseIds(dsl);

        HashSet<Tuple2<Long,Long>> databaseAppMappings = new HashSet<>();

        IntStream.range(0, 20_000)
                .forEach(i -> {
                    Long dbId = randomPick(databaseIds);
                    Long appId = randomPick(appIds);
                    databaseAppMappings.add(Tuple.tuple(dbId, appId));
                });

        List<DatabaseUsageRecord> databaseUsage = databaseAppMappings
                .stream()
                .map(t -> mkDatabaseUsageRecord(t.v1, t.v2))
                .collect(Collectors.toList());

        dsl.batchInsert(databaseUsage).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        System.out.println("-- deleting db records");
        getDsl(ctx)
                .deleteFrom(DATABASE_INFORMATION)
                .where(DATABASE_INFORMATION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }

    private static DatabaseUsageRecord mkDatabaseUsageRecord(long databaseId, long appId) {
        DatabaseUsageRecord r = new DatabaseUsageRecord();
        r.setDatabaseId(databaseId);
        r.setEntityKind(EntityKind.APPLICATION.name());
        r.setEntityId(appId);
        r.setEnvironment(randomPick(SampleData.environments));
        r.setLastUpdatedBy("admin");
        r.setProvenance(SAMPLE_DATA_PROVENANCE);
        return r;
    }


    private static List<Long> getDatabaseIds(DSLContext dsl) {
        return dsl.select(DATABASE_INFORMATION.ID)
                .from(DATABASE_INFORMATION)
                .fetch(DATABASE_INFORMATION.ID);
    }
}
