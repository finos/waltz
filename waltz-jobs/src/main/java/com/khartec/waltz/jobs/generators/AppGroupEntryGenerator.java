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

import com.khartec.waltz.schema.tables.records.ApplicationGroupEntryRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.common.RandomUtilities.randomlySizedIntStream;
import static com.khartec.waltz.schema.Tables.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;

public class AppGroupEntryGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);
        List<Long> groupIds = dsl
                .select(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .fetch(APPLICATION_GROUP.ID);


        List<ApplicationGroupEntryRecord> records = groupIds
                .stream()
                .flatMap(id -> randomlySizedIntStream(0, 25)
                        .mapToLong(idx -> randomPick(appIds))
                        .distinct()
                        .mapToObj(appId -> {
                            ApplicationGroupEntryRecord record = dsl.newRecord(APPLICATION_GROUP_ENTRY);
                            record.setGroupId(id);
                            record.setApplicationId(appId);
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        // handled in appgroup
        return false;
    }
}
