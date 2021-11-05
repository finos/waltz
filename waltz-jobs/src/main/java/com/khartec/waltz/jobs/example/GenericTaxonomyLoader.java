/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Waltz open source project
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

package com.khartec.waltz.jobs.example;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.StringUtilities;
import com.khartec.waltz.jobs.Columns;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.IOUtilities.getFileResource;
import static com.khartec.waltz.schema.Tables.*;

@Service
public class GenericTaxonomyLoader {

    private final DSLContext dsl;

    public static void main(String[] args) throws Exception {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        GenericTaxonomyLoader importer = ctx.getBean(GenericTaxonomyLoader.class);

        GenericTaxonomyLoadConfig config = ImmutableGenericTaxonomyLoadConfig.builder()
                .resourcePath("taxonomies/arch-review.tsv")
                .descriptionOffset(Columns.C)
                .taxonomyDescription("Application Architecture Reviews")
                .taxonomyExternalId("APP_ARCH")
                .taxonomyName("Application Architecture Reviews")
                .maxLevels(2)
                .ratingSchemeId(1L)
                .build();

        importer.go(config);
    }

    @Autowired
    public GenericTaxonomyLoader(DSLContext dsl) {
        this.dsl = dsl;
    }


    public void go(GenericTaxonomyLoadConfig config) throws IOException {
        List<String> lines = readLines(config);

        dsl.transaction(ctx -> {

            DSLContext tx = ctx.dsl();

            scrub(config, tx);

            Long categoryId = createCategory(config, tx);

            Timestamp creationTime = DateTimeUtilities.nowUtcTimestamp();

            int[] insertRcs = lines
                    .stream()
                    .filter(StringUtilities::notEmpty)
                    .map(line -> line.split("\\t"))
                    .flatMap(cells -> Stream.of(
                            mkMeasurableRecord(categoryId, cells[0], "", null, creationTime),
                            mkMeasurableRecord(categoryId, cells[1], cells[2], cells[0], creationTime)))
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), tx::batchInsert))
                    .execute();

            System.out.printf("Inserted %d records\n", insertRcs.length);

//            throw new RuntimeException("BOOooOOM!");
        });

    }


    private MeasurableRecord mkMeasurableRecord(Long categoryId, String name, String desc, String parentName, Timestamp creationTime) {
        MeasurableRecord r = new MeasurableRecord();
        r.setMeasurableCategoryId(categoryId);
        r.setName(name);
        r.setDescription(desc);
        r.setConcrete(true);
        r.setExternalId(toExtId(name));
        r.setLastUpdatedAt(creationTime);
        r.setLastUpdatedBy("admin");
        r.setProvenance("SAMPLE");

        if (parentName != null) {
            r.setExternalParentId(toExtId(parentName));
        }

        return r;
    }


    private String toExtId(String s) {
        return StringUtilities.mkSafe(s);
    }


    private Long createCategory(GenericTaxonomyLoadConfig config, DSLContext tx) {
        MeasurableCategoryRecord categoryRecord = tx.newRecord(MEASURABLE_CATEGORY);
        categoryRecord.setDescription(config.taxonomyDescription());
        categoryRecord.setName(config.taxonomyName());
        categoryRecord.setExternalId(config.taxonomyExternalId());
        categoryRecord.setRatingSchemeId(config.ratingSchemeId());

        categoryRecord.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        categoryRecord.setEditable(true);
        categoryRecord.setLastUpdatedBy("admin");

        categoryRecord.insert();
        return categoryRecord.getId();
    }


    private void scrub(GenericTaxonomyLoadConfig config, DSLContext tx) {
        tx.select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(config.taxonomyExternalId()))
                .fetchOptional(MEASURABLE_CATEGORY.ID)
                .ifPresent(catId -> {
                    tx.deleteFrom(MEASURABLE_RATING)
                        .where(MEASURABLE_RATING.MEASURABLE_ID.in(DSL
                                .select(MEASURABLE.ID)
                                .from(MEASURABLE)
                                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(catId))))
                        .execute();

                    tx.deleteFrom(MEASURABLE)
                        .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(catId))
                        .execute();

                    tx.deleteFrom(MEASURABLE_CATEGORY)
                        .where(MEASURABLE_CATEGORY.ID.eq(catId))
                        .execute();
                });
    }

    private List<String> readLines(GenericTaxonomyLoadConfig config) throws IOException {
        Resource resource = getFileResource(config.resourcePath());
        List<String> lines = IOUtilities.readLines(resource.getInputStream());
        return lines;
    }


}
