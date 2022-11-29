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

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.ComplexityKindRecord;
import org.finos.waltz.schema.tables.records.ComplexityRecord;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.schema.Tables.COMPLEXITY;
import static org.finos.waltz.schema.Tables.COMPLEXITY_KIND;
import static org.finos.waltz.schema.tables.Application.APPLICATION;

public class ComplexityGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = ctx.getBean(DSLContext.class);

        ComplexityKindRecord codeComplexity = dsl.newRecord(COMPLEXITY_KIND);
        codeComplexity.setName("Code Complexity");
        codeComplexity.setDescription("Code complexity measurement");
        codeComplexity.setExternalId("CODE");
        codeComplexity.setIsDefault(true);
        codeComplexity.insert();

        ComplexityKindRecord interfaceComplexity = dsl.newRecord(COMPLEXITY_KIND);
        interfaceComplexity.setName("Interface Complexity");
        interfaceComplexity.setDescription("Interface complexity measurement");
        interfaceComplexity.setExternalId("INTERFACE");
        interfaceComplexity.setIsDefault(true);
        interfaceComplexity.insert();

        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        int[] rcs = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .fetch(APPLICATION.ID)
                .stream()
                .flatMap(appId -> {
                    ComplexityRecord r1 = dsl.newRecord(Tables.COMPLEXITY);
                    r1.setComplexityKindId(codeComplexity.getId());
                    r1.setEntityId(appId);
                    r1.setEntityKind(EntityKind.APPLICATION.name());
                    r1.setScore(BigDecimal.valueOf(RandomUtilities.getRandom().nextDouble()));
                    r1.setLastUpdatedBy("test");
                    r1.setLastUpdatedAt(now);
                    r1.setProvenance("test");

                    ComplexityRecord r2 = dsl.newRecord(Tables.COMPLEXITY);
                    r2.setComplexityKindId(interfaceComplexity.getId());
                    r2.setEntityId(appId);
                    r2.setEntityKind(EntityKind.APPLICATION.name());
                    r2.setScore(BigDecimal.valueOf(RandomUtilities.getRandom().nextDouble()));
                    r2.setLastUpdatedBy("test");
                    r2.setLastUpdatedAt(now);
                    r2.setProvenance("test");

                    return Stream.of(r1, r2);
                })
                .collect(Collectors.collectingAndThen(toSet(), dsl::batchInsert))
                .execute();

        log("Inserted " + rcs.length + " complexity entries");
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(COMPLEXITY)
                .execute();
        getDsl(ctx)
                .deleteFrom(COMPLEXITY_KIND)
                .execute();
        return false;
    }


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        LoggingUtilities.configureLogging();
        ComplexityGenerator generator = new ComplexityGenerator();
        generator.remove(ctx);
        generator.create(ctx);
    }
}
