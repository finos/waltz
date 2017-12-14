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

import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.common.StringUtilities.notEmpty;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static java.util.stream.Collectors.*;

/**
 * Created by dwatkins on 30/12/2016.
 */
public class MeasurablesGenerator {
    private static final String REGION_CATEGORY_EXTERNAL_ID     = "REGION";


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        generateRegions(dsl);
    }


    private static void generateRegions(DSLContext dsl) throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/regions.csv"));

        System.out.println("Deleting existing Regions & Countries ...");
        int deletedCount = dsl
                .deleteFrom(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.in(DSL
                        .select(MEASURABLE_CATEGORY.ID)
                        .from(MEASURABLE_CATEGORY)
                        .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(REGION_CATEGORY_EXTERNAL_ID))))
                .and(MEASURABLE.PROVENANCE.eq("demo"))
                .execute();
        System.out.println("Deleted: " + deletedCount + " existing Regions & Countries");

        Map<String, Map<String, Set<String>>> regionHierarchy = lines.stream()
                .skip(1)
                .map(line -> StringUtils.splitPreserveAllTokens(line, ","))
                .filter(cells -> notEmpty(cells[0]) && notEmpty(cells[6]) && notEmpty(cells[5]))
                .map(cells -> Tuple.tuple(cells[0], cells[6], cells[5]))
                .collect(groupingBy(
                        t -> t.v3,
                        groupingBy(t -> t.v2, mapping(t -> t.v1, toSet()))
                ));

        final long measurableCategoryId = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(REGION_CATEGORY_EXTERNAL_ID))
                .fetchAny()
                .value1();

        AtomicInteger insertCount = new AtomicInteger(0);
        regionHierarchy.forEach((region, subRegionMap) -> {
            final long regionId = dsl
                    .insertInto(MEASURABLE)
                    .set(createRegion(null, region, measurableCategoryId, false))
                    .returning(MEASURABLE.ID)
                    .fetchOne()
                    .getId();
            insertCount.incrementAndGet();

            subRegionMap.forEach((subRegion, countries) -> {
                final long subRegionId = dsl
                        .insertInto(MEASURABLE)
                        .set(createRegion(regionId, subRegion, measurableCategoryId, true))
                        .returning(MEASURABLE.ID)
                        .fetchOne()
                        .getId();
                insertCount.incrementAndGet();

                insertCount.addAndGet(dsl.batchInsert(countries
                        .stream()
                        .map(country -> createRegion(subRegionId, country, measurableCategoryId, true))
                        .collect(toList()))
                        .execute().length);
            });
        });

        System.out.println("Inserted: " + insertCount + " Regions & Countries");
    }


    private static MeasurableRecord createRegion(Long parentId,
                                                 String name,
                                                 Long measurableCategoryId,
                                                 Boolean concrete) {
        MeasurableRecord record = new MeasurableRecord();
        if (parentId != null) {
            record.setParentId(parentId);
        }
        record.setName(name);
        record.setMeasurableCategoryId(measurableCategoryId);
        record.setConcrete(concrete);
        record.setDescription("");
        record.setLastUpdatedBy("");
        record.setProvenance("demo");

        return record;
    }
}
