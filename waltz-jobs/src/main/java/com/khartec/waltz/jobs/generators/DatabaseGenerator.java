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
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.schema.tables.records.DatabaseInformationRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;

public class DatabaseGenerator implements SampleDataGenerator {


    private final Random rnd = RandomUtilities.getRandom();


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> codes = dsl
                .select(APPLICATION.ASSET_CODE)
                .from(APPLICATION)
                .fetch(APPLICATION.ASSET_CODE);

        List<DatabaseInformationRecord> databaseRecords = new LinkedList<>();

        for (int i = 0; i < 150; i++) {
            int num = rnd.nextInt(10);

            for (int j = 0 ; j < num; j++) {
                SoftwarePackage pkg = randomPick(DatabaseSoftwarePackages.dbs);

                DatabaseInformationRecord databaseRecord = dsl.newRecord(DATABASE_INFORMATION);
                databaseRecord.setDatabaseName("DB_LON_" + i + "_" + j);
                databaseRecord.setInstanceName("DB_INST_" + i);
                databaseRecord.setEnvironment(randomPick("PROD", "PROD", "QA", "DEV", "DEV"));
                databaseRecord.setDbmsVendor(pkg.vendor());
                databaseRecord.setDbmsName(pkg.name());
                databaseRecord.setExternalId("ext_" + i + "_" +j);
                databaseRecord.setProvenance("RANDOM_GENERATOR");
                databaseRecord.setAssetCode(randomPick(codes));
                databaseRecord.setLifecycleStatus(randomPick(LifecycleStatus.values()).toString());
                databaseRecord.setEndOfLifeDate(
                        rnd.nextInt(10) > 5
                                ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                                : null);

                databaseRecords.add(databaseRecord);
            }
        }

        // insert duplicate database instance records (more than one app using the same database)
        SoftwarePackage dupDbPackage = randomPick(DatabaseSoftwarePackages.dbs);
        String dupDbEnvironment = randomPick("PROD", "PROD", "QA", "DEV", "DEV");
        Date dupDbEolDate = rnd.nextInt(10) > 5
                ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                : null;
        for (int i = 0; i < 10; i++) {
            DatabaseInformationRecord databaseRecord = dsl.newRecord(DATABASE_INFORMATION);
            databaseRecord.setDatabaseName("DB_LON_REF_DATA");
            databaseRecord.setInstanceName("DB_INST_REF_DATA");
            databaseRecord.setEnvironment(dupDbEnvironment);
            databaseRecord.setDbmsVendor(dupDbPackage.vendor());
            databaseRecord.setDbmsName(dupDbPackage.name());
            databaseRecord.setExternalId("ext_ref_data");
            databaseRecord.setProvenance("RANDOM_GENERATOR");
            databaseRecord.setAssetCode(randomPick(codes));
            databaseRecord.setLifecycleStatus(randomPick(LifecycleStatus.values()).toString());
            databaseRecord.setEndOfLifeDate(
                    dupDbEolDate);

            databaseRecords.add(databaseRecord);
        }


        log("-- storing db records ( " + databaseRecords.size() + " )");
        databaseRecords.forEach(r -> r.store());
        log("-- done inserting db records");

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
}
