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

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static java.util.stream.Collectors.toList;


public class MeasurableRelationshipsGenerator {
    private static final String PROVENANCE = "DEMO";

    private static final String REGION_CATEGORY_EXTERNAL_ID     = "REGION";
    private static final String FUNCTION_CATEGORY_EXTERNAL_ID   = "CAPABILITY";


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        MeasurableDao measurableDao = ctx.getBean(MeasurableDao.class);

        List<Measurable> regions = loadMeasurablesForCategory(measurableDao, REGION_CATEGORY_EXTERNAL_ID);
        List<Measurable> functions = loadMeasurablesForCategory(measurableDao, FUNCTION_CATEGORY_EXTERNAL_ID);

        final int maxRelationshipCount = 50;
        final int minRelationshipCount = 5;
        final Random random = new Random();

        List<EntityRelationshipRecord> relationshipRecords = functions.stream()
                .flatMap(function -> {
                    int relationshipCount = random.nextInt(maxRelationshipCount - minRelationshipCount) + minRelationshipCount;
                    return IntStream.range(0, relationshipCount)
                            .mapToObj(i -> ListUtilities.randomPick(regions))
                            .distinct()
                            .map(region -> creatRelationshipRecord(function, region, ArrayUtilities.randomPick(
                                    RelationshipKind.RELATES_TO)));
                })
                .collect(toList());

        System.out.println("Deleting existing Measurable relationships ...");
        int deleteCount = dsl.deleteFrom(ENTITY_RELATIONSHIP)
                .where(ENTITY_RELATIONSHIP.PROVENANCE.eq(PROVENANCE)
                        .and(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.MEASURABLE.name()))
                        .and(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.MEASURABLE.name())))
                .execute();
        System.out.println("Deleted: " + deleteCount + " existing Measurable relationships");

        System.out.println("Inserting Measurable relationships ...");
        int[] insertCount = dsl.batchInsert(relationshipRecords).execute();
        System.out.println("Inserted: " + insertCount.length + " Measurable relationships ...");
    }


    private static List<Measurable> loadMeasurablesForCategory(MeasurableDao measurableDao,
                                                               String measurableCategoryExternalId) {
        return measurableDao.findByMeasurableIdSelector(DSL
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .join(MEASURABLE_CATEGORY).on(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(MEASURABLE_CATEGORY.ID))
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(measurableCategoryExternalId)));
    }


    private static EntityRelationshipRecord creatRelationshipRecord(Measurable function,
                                                                    Measurable region,
                                                                    RelationshipKind relationshipKind) {
        EntityRelationshipRecord record = new EntityRelationshipRecord();
        record.setIdA(function.id().get());
        record.setKindA(EntityKind.MEASURABLE.name());
        record.setIdB(region.id().get());
        record.setKindB(EntityKind.MEASURABLE.name());
        record.setRelationship(relationshipKind.name());
        record.setProvenance(PROVENANCE);

        return record;
    }
}
