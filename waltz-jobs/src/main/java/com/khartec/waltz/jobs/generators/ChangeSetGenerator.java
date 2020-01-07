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

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.schema.tables.records.ChangeSetRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.ChangeSet.CHANGE_SET;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static java.lang.String.format;

public class ChangeSetGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static final String[] names = {
            "BCBS Lineage Tracker Changes",
            "Patch release",
            "Strategy .Next",
            "Decomm feeds",
            "Flow update",
    };

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        LocalDateTime now = LocalDateTime.now();

        List<Long> ciIds = dsl
                .select(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .fetch(CHANGE_INITIATIVE.ID);

        AtomicInteger counter = new AtomicInteger(0);

        List<ChangeSetRecord> groupRecords = Arrays
                .stream(names)
                .map(n -> {
                    ChangeSetRecord record = dsl.newRecord(CHANGE_SET);
                    record.setParentEntityKind(EntityKind.CHANGE_INITIATIVE.name());
                    record.setParentEntityId(randomPick(ciIds));
                    record.setPlannedDate(Timestamp.valueOf(now));
                    record.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE.name());
                    record.setName(n);
                    record.setDescription(format("%s : Description of %s", SAMPLE_DATA_PROVENANCE, n));
                    record.setLastUpdatedAt(Timestamp.valueOf(now));
                    record.setLastUpdatedBy("admin");
                    record.setExternalId(format("change-set-ext-%s", counter.addAndGet(1)));
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        dsl.batchStore(groupRecords).execute();

        return null;

    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        dsl.deleteFrom(CHANGE_SET)
           .where(CHANGE_SET.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
           .execute();

        dsl.deleteFrom(INVOLVEMENT)
           .where(INVOLVEMENT.ENTITY_KIND.in(EntityKind.CHANGE_SET.name(), EntityKind.CHANGE_UNIT.name()))
           .execute();
        return true;
    }

}
