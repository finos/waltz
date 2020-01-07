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

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.AuthoritativeSourceRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

/**
 * Created by dwatkins on 04/03/2017.
 */
public class AuthSourceGenerator implements SampleDataGenerator {


    private static final Random rnd = RandomUtilities.getRandom();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);

        List<Long> ouIds = dsl
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(ORGANISATIONAL_UNIT.ID);

        List<String> types = dsl
                .select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .fetch(DATA_TYPE.CODE);

        List<AuthoritativeSourceRecord> records = types.stream()
                .flatMap(t -> IntStream
                        .range(0, 2 + rnd.nextInt(2))
                        .mapToObj(i -> {
                            AuthoritativeSourceRecord record = dsl.newRecord(AUTHORITATIVE_SOURCE);
                            record.setDataType(t);
                            record.setRating(randomPick(AuthoritativenessRating.PRIMARY, AuthoritativenessRating.SECONDARY).name());
                            record.setApplicationId(randomPick(appIds));
                            record.setParentId(randomPick(ouIds));
                            record.setParentKind(EntityKind.ORG_UNIT.name());
                            record.setProvenance(SAMPLE_DATA_PROVENANCE);
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(AUTHORITATIVE_SOURCE)
                .where(AUTHORITATIVE_SOURCE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }
}
