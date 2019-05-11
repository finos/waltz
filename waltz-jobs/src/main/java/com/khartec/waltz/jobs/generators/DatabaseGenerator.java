/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
                databaseRecord.setDbmsVersion(pkg.version());
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
            databaseRecord.setDbmsVersion(dupDbPackage.version());
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
