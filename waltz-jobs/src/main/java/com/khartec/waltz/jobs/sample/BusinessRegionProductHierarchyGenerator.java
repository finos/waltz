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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.common.StringUtilities.notEmpty;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static java.util.stream.Collectors.*;

public class BusinessRegionProductHierarchyGenerator {
    private static final String CATEGORY_EXTERNAL_ID = "BUSINESS_REGION_PRODUCT";

    private static String[] businesses = new String[]{
            "Asset Management",
            "Equities",
            "Fixed Income",
            "Retail Banking",
    };

    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        generateHierarchy(dsl);
    }

    private static void generateHierarchy(DSLContext dsl) throws IOException {
        System.out.println("Deleting existing  hierarchy ...");
        int deletedCount = dsl
                .deleteFrom(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.in(DSL
                        .select(MEASURABLE_CATEGORY.ID)
                        .from(MEASURABLE_CATEGORY)
                        .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(CATEGORY_EXTERNAL_ID))))
                .and(MEASURABLE.PROVENANCE.eq("demo"))
                .execute();
        System.out.println("Deleted: " + deletedCount + " existing hierarchy");

        Set<String> topLevelRegions = readRegions();
        Set<String> products = readProducts();

        insertCategoryIfNotExists(dsl);

        final long measurableCategoryId = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(CATEGORY_EXTERNAL_ID))
                .fetchAny()
                .value1();

        AtomicInteger insertCount = new AtomicInteger(0);


        Stream.of(businesses).forEach(business -> {
                final long businessId = dsl
                        .insertInto(MEASURABLE)
                        .set(createMeasurable(null, business, measurableCategoryId, true))
                        .returning(MEASURABLE.ID)
                        .fetchOne()
                        .getId();

                insertCount.incrementAndGet();

                topLevelRegions.forEach((region) -> {
                    final long regionId = dsl
                            .insertInto(MEASURABLE)
                            .set(createMeasurable(businessId, region, measurableCategoryId, true))
                            .returning(MEASURABLE.ID)
                            .fetchOne()
                            .getId();
                    insertCount.incrementAndGet();

                    products.forEach(product -> {
                        dsl
                                .insertInto(MEASURABLE)
                                .set(createMeasurable(regionId, product, measurableCategoryId, true))
                                .execute();

                        insertCount.incrementAndGet();
                    });
                });
        });

        System.out.println("Inserted: " + insertCount + " new nodes for hierarchy");
    }


    private static Set<String> readRegions() throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/regions.csv"));
        Map<String, Map<String, Set<String>>> regionHierarchy = lines.stream()
                .skip(1)
                .map(line -> StringUtils.splitPreserveAllTokens(line, ","))
                .filter(cells -> notEmpty(cells[0]) && notEmpty(cells[6]) && notEmpty(cells[5]))
                .map(cells -> Tuple.tuple(cells[0], cells[6], cells[5]))
                .collect(groupingBy(
                        t -> t.v3,
                        groupingBy(t -> t.v2, mapping(t -> t.v1, toSet()))
                ));

        return regionHierarchy.keySet();
    }


    private static Set<String> readProducts() throws IOException {
        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/products-flat.csv"));
        Set<String> productHierarchy = lines.stream()
                .skip(1)
                .map(line -> StringUtils.splitPreserveAllTokens(line, ","))
                .filter(cells -> notEmpty(cells[0]))
                .map(cells -> cells[0])
                .collect(toSet());

        return productHierarchy;
    }


    private static MeasurableRecord createMeasurable(Long parentId,
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


    private static void insertCategoryIfNotExists(DSLContext dsl) {
        Record1<Long> longRecord1 = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(CATEGORY_EXTERNAL_ID))
                .fetchAny();

        if(longRecord1 == null) {
            MeasurableCategoryRecord mcr = new MeasurableCategoryRecord();
            mcr.setName("Region Business Products");
            mcr.setDescription("Region Business Products Hierarchy");
            mcr.setExternalId(CATEGORY_EXTERNAL_ID);
            mcr.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
            mcr.setLastUpdatedBy("admin");

            dsl.insertInto(MEASURABLE_CATEGORY)
                    .set(mcr)
                    .execute();
        }
    }
}
