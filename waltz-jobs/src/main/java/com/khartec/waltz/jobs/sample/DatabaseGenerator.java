package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.jobs.sample.software_packages.DatabaseSoftwarePackages;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.schema.tables.records.DatabaseInformationRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;

public class DatabaseGenerator {

    private static final Random rnd = new Random();

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> codes = dsl
                .select(APPLICATION.ASSET_CODE)
                .from(APPLICATION)
                .fetch(APPLICATION.ASSET_CODE);

        List<DatabaseInformationRecord> databaseRecords = new LinkedList<>();

        for (int i = 0; i < 300; i++) {
            int num = rnd.nextInt(10);

            for (int j = 0 ; j < num; j++) {
                SoftwarePackage pkg = ArrayUtilities.randomPick(DatabaseSoftwarePackages.dbs);

                DatabaseInformationRecord databaseRecord = dsl.newRecord(DATABASE_INFORMATION);
                databaseRecord.setDatabaseName("DB_LON_" + i + "_" + j);
                databaseRecord.setInstanceName("DB_INST_" + i);
                databaseRecord.setEnvironment(ArrayUtilities.randomPick("PROD", "PROD", "QA", "DEV", "DEV"));
                databaseRecord.setDbmsVendor(pkg.vendor());
                databaseRecord.setDbmsName(pkg.name());
                databaseRecord.setDbmsVersion(pkg.version());
                databaseRecord.setExternalId("ext_" + i + "_" +j);
                databaseRecord.setProvenance("RANDOM_GENERATOR");
                databaseRecord.setAssetCode(randomPick(codes));
                databaseRecord.setEndOfLifeDate(
                        rnd.nextInt(10) > 5
                            ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                            : null);

                databaseRecords.add(databaseRecord);
            }
        }

        System.out.println("-- deleting db records");
        dsl.deleteFrom(DATABASE_INFORMATION)
                .where(DATABASE_INFORMATION.PROVENANCE.eq("RANDOM_GENERATOR"))
                .execute();
        System.out.println("-- storing db records ( " + databaseRecords.size() + " )");
        databaseRecords.forEach(r -> r.store());
        System.out.println("-- done inserting db records");

        System.out.println("-- done");



    }
}
