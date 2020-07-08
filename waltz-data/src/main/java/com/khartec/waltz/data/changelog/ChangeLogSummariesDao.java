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

package com.khartec.waltz.data.changelog;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tally.ChangeLogTally;
import com.khartec.waltz.model.tally.DateTally;
import com.khartec.waltz.model.tally.ImmutableChangeLogTally;
import com.khartec.waltz.model.tally.ImmutableDateTally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;


@Repository
public class ChangeLogSummariesDao {

    private final DSLContext dsl;


    private static final RecordMapper<Record2<Date,Integer>, DateTally> TO_DATE_TALLY_MAPPER = record -> {
        Date date = record.value1();
        Integer count = record.value2();

        return ImmutableDateTally.builder()
                .date(date)
                .count(count)
                .build();
    };


    private static final RecordMapper<Record4<Long, String, String, Integer>, ChangeLogTally> TO_CHANGE_LOG_TALLY_MAPPER = record -> {

        EntityKind parentKind = EntityKind.valueOf(record.value2());
        EntityKind childKind = (record.value3() != null) ? EntityKind.valueOf(record.value3()) : null;
        Integer count = record.value4();

        return ImmutableChangeLogTally.builder()
                .ref(EntityReference.mkRef(parentKind, record.value1()))
                .childKind(childKind)
                .count(count)
                .build();
    };


    @Autowired
    public ChangeLogSummariesDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<DateTally> findCountByDateForParentKindBySelector(GenericSelector selector,
                                                                  Optional<Integer> limit) {
        checkNotNull(selector, "selector must not be null");

        Field<Date> date = DSL.date(CHANGE_LOG.CREATED_AT);

        return dsl.select(date, DSL.count(CHANGE_LOG.ID))
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.in(selector.selector())
                .and(CHANGE_LOG.PARENT_KIND.eq(selector.kind().name())))
                .groupBy(date)
                .orderBy(date.desc())
                .limit(limit.orElse(365))
                .fetch(TO_DATE_TALLY_MAPPER);
    }


    public List<ChangeLogTally> findCountByParentAndChildKindForDateBySelector(EntityKind parentKind,
                                                                               Select<Record1<Long>> selector,
                                                                               Date date,
                                                                               Optional<Integer> limit) {
        checkNotNull(parentKind, "parentKind must not be null");

        AggregateFunction<Integer> count = DSL.count(CHANGE_LOG.ID);
        Field<Date> changelogDate = DSL.date(CHANGE_LOG.CREATED_AT);

        return dsl.select(CHANGE_LOG.PARENT_ID, CHANGE_LOG.PARENT_KIND, CHANGE_LOG.CHILD_KIND, count)
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.in(selector)
                        .and(CHANGE_LOG.PARENT_KIND.eq(parentKind.name()))
                        .and(changelogDate.eq(date)))
                .groupBy(CHANGE_LOG.PARENT_ID, CHANGE_LOG.PARENT_KIND, CHANGE_LOG.CHILD_KIND)
                .orderBy(count.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_CHANGE_LOG_TALLY_MAPPER);
    }
}
