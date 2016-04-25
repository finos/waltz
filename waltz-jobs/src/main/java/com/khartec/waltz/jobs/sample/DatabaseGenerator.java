package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.jobs.sample.software_packages.DatabaseSoftwarePackages;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entiy_relationship.RelationshipKind;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.schema.tables.records.DatabaseRecord;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Database.DATABASE;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;

public class DatabaseGenerator {

    private static final Random rnd = new Random();

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> ids = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .fetch(APPLICATION.ID);

        List<DatabaseRecord> databaseRecords = new LinkedList<>();

        for (int i = 0; i < 300; i++) {
            int num = rnd.nextInt(10);

            for (int j = 0 ; j < num; j++) {
                SoftwarePackage pkg = ArrayUtilities.randomPick(DatabaseSoftwarePackages.dbs);

                DatabaseRecord databaseRecord = dsl.newRecord(DATABASE);
                databaseRecord.setDatabaseName("DB_LON_" + i + "_" + j);
                databaseRecord.setInstanceName("DB_INST_" + i);
                databaseRecord.setEnvironment(ArrayUtilities.randomPick("PROD", "PROD", "QA", "DEV", "DEV"));
                databaseRecord.setDbmsVendor(pkg.vendor());
                databaseRecord.setDbmsName(pkg.name());
                databaseRecord.setDbmsVersion(pkg.version());
                databaseRecord.setExternalId("ext_" + i + "_" +j);
                databaseRecord.setProvenance("RANDOM_GENERATOR");

                databaseRecords.add(databaseRecord);
            }
        }

        System.out.println("-- deleting db records");
        dsl.deleteFrom(DATABASE)
                .where(DATABASE.PROVENANCE.eq("RANDOM_GENERATOR"))
                .execute();
        System.out.println("-- storing db records ( " + databaseRecords.size() + " )");
        databaseRecords.forEach(r -> r.store());
        System.out.println("-- done inserting db records");


        List<EntityRelationshipRecord> relationshipRecords = databaseRecords.stream()
                .map(r -> r.getId())
                .map(dbId -> {
                    EntityRelationshipRecord relationshipRecord = new EntityRelationshipRecord();
                    relationshipRecord.setKindA(EntityKind.APPLICATION.name());
                    relationshipRecord.setKindB(EntityKind.DATABASE.name());

                    relationshipRecord.setIdA(ListUtilities.randomPick(ids));
                    relationshipRecord.setIdB(dbId);
                    relationshipRecord.setProvenance("RANDOM_GENERATOR");
                    relationshipRecord.setRelationship(RelationshipKind.HAS.name());

                    return relationshipRecord;
                })
                .collect(Collectors.toList());


        System.out.println("-- deleting rel records");
        dsl.deleteFrom(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.DATABASE.name()))
                .and(ENTITY_RELATIONSHIP.PROVENANCE.eq("RANDOM_GENERATOR"))
                .execute();
        System.out.println("-- storing rel records ( " + relationshipRecords.size() + " )");
        dsl.batchInsert(relationshipRecords).execute();
        System.out.println("-- done");



    }
}
