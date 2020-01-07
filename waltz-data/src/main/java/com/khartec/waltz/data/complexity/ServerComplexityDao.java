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

package com.khartec.waltz.data.complexity;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static com.khartec.waltz.schema.tables.ServerUsage.SERVER_USAGE;

@Repository
public class ServerComplexityDao {

    private static final Field<Integer> SERVER_COUNT_FIELD = DSL.field(DSL.count().as("server_count"));

    private final DSLContext dsl;


    @Autowired
    public ServerComplexityDao(DSLContext dsl) {
        this.dsl = dsl;
    }



    public List<Tally<String>> findCountsByAssetCodes(String... assetCodes) {
        return findCountsByAssetCodes(APPLICATION.ASSET_CODE.in(assetCodes));
    }


    public int calculateBaseline() {
        return calculateBaseline(DSL.trueCondition());
    }


    public List<Tally<String>> findCounts() {
        return findCountsByAssetCodes(DSL.trueCondition());
    }


    public List<Tally<Long>> findCountsByAppIdSelector(Select<Record1<Long>> appIdSelector) {
        Checks.checkNotNull(appIdSelector, "appIdSelector cannot be null");

        return dsl.select(SERVER_USAGE.ENTITY_ID, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .innerJoin(SERVER_USAGE)
                        .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .where(SERVER_USAGE.ENTITY_ID.in(appIdSelector))
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .groupBy(SERVER_USAGE.ENTITY_ID)
                .fetch(r -> ImmutableTally.<Long>builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());
    }


    private List<Tally<String>> findCountsByAssetCodes(Condition condition) {
        Checks.checkNotNull(condition, "Condition must be given, use DSL.trueCondition() for 'none'");

        return dsl.select(APPLICATION.ASSET_CODE, SERVER_COUNT_FIELD)
                .from(SERVER_INFORMATION)
                .innerJoin(SERVER_USAGE)
                    .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
                    .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(condition)
                .groupBy(APPLICATION.ASSET_CODE)
                .orderBy(SERVER_COUNT_FIELD.desc())
                .fetch(r -> ImmutableTally.<String>builder()
                        .id(r.value1())
                        .count(r.value2())
                        .build());

    }


    private int calculateBaseline(Condition condition) {
        return dsl.select(DSL.max(SERVER_COUNT_FIELD))
                .from(DSL.select(SERVER_USAGE.ENTITY_ID, SERVER_COUNT_FIELD)
                        .from(SERVER_USAGE)
                        .where(condition)
                        .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .groupBy(SERVER_USAGE.ENTITY_ID))
                .fetchOne(Record1::value1);
    }

}
