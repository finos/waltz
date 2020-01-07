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

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.schema.tables.records.MeasurableCategoryRecord;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;

/**
 * Created by dwatkins on 04/03/2017.
 */
public class MeasurableGenerator implements SampleDataGenerator {

    private final String category;

    public MeasurableGenerator(String category) {
        checkNotEmpty(category, "category cannot be null");
        this.category = category;
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        System.out.println("Importing data for category: " + category);
        Supplier<List<String>> lineSupplier = Unchecked.supplier(() -> readLines(getClass().getResourceAsStream("/" + category + ".tsv")));

        long categoryId = getCategory(dsl);

        List<String> lines = lineSupplier.get();

        List<String[]> sheet = lineSupplier
                .get()
                .stream()
                .skip(1)
                .map(line -> line.split("\\t"))
                .filter(cells -> cells.length > 3)
                .collect(Collectors.toList());

        List<MeasurableRecord> records = sheet
                .stream()
                .map(cells -> {
                    MeasurableRecord record = dsl.newRecord(MEASURABLE);
                    record.setName(cells[2]);
                    record.setDescription(cells[3]);
                    record.setMeasurableCategoryId(categoryId);
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setConcrete(! StringUtilities.isEmpty(cells[1]));
                    record.setExternalId(category + " _ " + cells[0]);
                    return record;
                })
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        sheet.stream()
                .filter(cells -> !StringUtilities.isEmpty(cells[1]))
                .forEach(cells -> {
                    String pExtId = cells[1];
                    System.out.println(Arrays.toString(cells));
                    long pId = dsl
                            .select(MEASURABLE.ID)
                            .from(MEASURABLE)
                            .where(MEASURABLE.EXTERNAL_ID.eq(category + " _ " + pExtId))
                            .fetchOne(MEASURABLE.ID);
                    dsl.update(MEASURABLE)
                            .set(MEASURABLE.PARENT_ID, pId)
                            .where(MEASURABLE.EXTERNAL_ID.equal(category + " _ " + cells[0]))
                            .execute();
                });

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);
        long category = getCategory(dsl);

        deleteRatingsForCategory(dsl, category);

        dsl.deleteFrom(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(category))
                .execute();
        return true;
    }



    private long getCategory(DSLContext dsl) {
        SelectConditionStep<MeasurableCategoryRecord> findQuery = dsl
                .selectFrom(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(category));

        if (findQuery.fetchAny() == null) {
            dsl.insertInto(MEASURABLE_CATEGORY)
                    .set(MEASURABLE_CATEGORY.EXTERNAL_ID, category)
                    .set(MEASURABLE_CATEGORY.DESCRIPTION, category.toLowerCase())
                    .set(MEASURABLE_CATEGORY.NAME, initialiseFirst(category))
                    .set(MEASURABLE_CATEGORY.LAST_UPDATED_BY, "admin")
                    .set(MEASURABLE_CATEGORY.RATING_SCHEME_ID, 1L)
                    .execute();
        }

        return findQuery
                .fetchOne()
                .getId();
    }


    private String initialiseFirst(String s) {
        String lc = s.toLowerCase();
        return Character.toUpperCase(lc.charAt(0)) + lc.substring(1);
    }
}
