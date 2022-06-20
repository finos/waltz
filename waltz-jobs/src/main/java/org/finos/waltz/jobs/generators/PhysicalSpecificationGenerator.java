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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.records.PhysicalSpecificationRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class PhysicalSpecificationGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static String[] names = {
            "trade",
            "report",
            "risk",
            "ratings",
            "eod",
            "intra-day",
            "yyymmdd",
            "finance",
            "accounting",
            "balance",
            "agg",
            "holdings",
            "accruals",
            "debit",
            "credit",
            "currency",
            "regulatory",
            "transactions",
            "transfers",
            "exchange",
            "summary",
            "daily",
            "position",
            "settlement",
            "confirms",
            "confirmation"
    };


    private static String[] extensions = {
            "xls",
            "txt",
            "csv",
            "tsv",
            "psv",
            "md",
            "bin",
            "xml",
            "json",
            "yaml",
            "yml",
            "pdf",
            "rtf",
            "doc"
    };


    private static String mkName(Integer i) {

        return new StringBuilder()
                .append(randomPick(names))
                .append("-")
                .append(randomPick(names))
                .append("-")
                .append(i)
                .append(".")
                .append(randomPick(extensions))
                .toString();
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);

        List<PhysicalSpecificationRecord> records = appIds
                .stream()
                .flatMap(appId -> IntStream
                        .range(0, rnd.nextInt(4))
                        .mapToObj(i -> tuple(appId, i)))
                .map(t -> {
                    String name = mkName(t.v2);
                    PhysicalSpecificationRecord record = dsl.newRecord(PHYSICAL_SPECIFICATION);
                    record.setOwningEntityId(t.v1);
                    record.setOwningEntityKind(EntityKind.APPLICATION.name());
                    record.setFormat(randomPick("XML", "DATABASE", "JSON", "OTHER"));
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setDescription("Desc "+ name + " " + t.v2);
                    record.setName(name);
                    record.setExternalId("ext-" + t.v1 + "." + t.v2);
                    record.setLastUpdatedBy("admin");
                    record.setCreatedBy("admin");
                    record.setCreatedAt(nowUtcTimestamp());
                    return record;
                })
                .collect(Collectors.toList());



        log("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        log("---done");
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        log("---deleting old demo records");
        dsl.deleteFrom(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }
}
