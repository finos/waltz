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

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomIntBetween;
import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ChangeLogGenerator implements SampleDataGenerator {

    private static final Set<Tuple2<String, EntityKind>> messages = SetUtilities.asSet(
            tuple("Updated the application", EntityKind.APPLICATION),
            tuple("Modified the application", EntityKind.APPLICATION),
            tuple("Enriched the application", EntityKind.APPLICATION),
            tuple("Added to the application", EntityKind.APPLICATION),
            tuple("Removed data from the application", EntityKind.APPLICATION),
            tuple("Added flow", EntityKind.LOGICAL_DATA_FLOW),
            tuple("Removed flow", EntityKind.LOGICAL_DATA_FLOW),
            tuple("Added Physical flow", EntityKind.PHYSICAL_FLOW),
            tuple("Removed Physical flow", EntityKind.PHYSICAL_FLOW));


    private static ChangeLogRecord mkChangeLog(long appId, String email, LocalDateTime when) {
        Tuple2<String, EntityKind> messageTemplate = randomPick(messages);

        ChangeLogRecord record = new ChangeLogRecord();
        record.setMessage(messageTemplate.v1);
        record.setParentId(appId);
        record.setParentKind(EntityKind.APPLICATION.name());
        record.setChildKind(messageTemplate.v2.name());
        record.setUserId(email);
        record.setSeverity(Severity.INFORMATION.name());
        record.setCreatedAt(Timestamp.valueOf(when));

        return record;
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = ctx.getBean(DSLContext.class);

        // get applications and emails
        List<Long> appIds = dsl.select(APPLICATION.ID)
                .from(APPLICATION)
                .fetch(APPLICATION.ID);

        List<String> emails = dsl.select(PERSON.EMAIL)
                .from(PERSON)
                .fetch(PERSON.EMAIL);

        Set<ChangeLogRecord> records = emails.stream()
                .flatMap(email -> {
                    Long appId = randomPick(appIds);
                    LocalDateTime when = now().minus(randomIntBetween(0, 365), ChronoUnit.DAYS);
                    return Stream.of(
                            mkChangeLog(appId, email, when),
                            mkChangeLog(appId, email, when),
                            mkChangeLog(randomPick(appIds), email, when),
                            mkChangeLog(appId, randomPick(emails), when));
                })
                .collect(toSet());

        dsl.batchInsert(records).execute();

        log("Inserted " + records.size() + " change log entries");
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(CHANGE_LOG)
                .execute();
        return false;
    }
}
