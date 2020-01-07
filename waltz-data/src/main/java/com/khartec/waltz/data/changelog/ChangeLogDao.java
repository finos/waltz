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

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.tally.OrderedTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.data.JooqUtilities.*;
import static com.khartec.waltz.schema.Tables.PERSON;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;


@Repository
public class ChangeLogDao {

    private final DSLContext dsl;

    public static final RecordMapper<? super Record, ChangeLog> TO_DOMAIN_MAPPER = r -> {
        ChangeLogRecord record = r.into(ChangeLogRecord.class);

        EntityReference parentRef = ImmutableEntityReference.builder()
                .id(record.getParentId())
                .kind(EntityKind.valueOf(record.getParentKind()))
                .build();

        return ImmutableChangeLog.builder()
                .userId(record.getUserId())
                .message(record.getMessage())
                .severity(Severity.valueOf(record.getSeverity()))
                .parentReference(parentRef)
                .childKind(Optional.ofNullable(record.getChildKind()).map(ck -> EntityKind.valueOf(ck)))
                .operation(Operation.valueOf(record.getOperation()))
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .build();
    };


    @Autowired
    public ChangeLogDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()))
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeLog> findByPersonReference(EntityReference ref,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        SelectConditionStep<Record> byParentRef = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()));

        SelectConditionStep<Record> byUserId = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .innerJoin(PERSON).on(PERSON.EMAIL.eq(CHANGE_LOG.USER_ID))
                .where(PERSON.ID.eq(ref.id()));

        SelectOrderByStep<Record> union = byParentRef.unionAll(byUserId);

        return dsl
                .select(union.fields())
                .from(union.asTable())
                .orderBy(union.field("created_at").desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeLog> findByUser(String userName,
                                      Optional<Integer> limit) {
        checkNotEmpty(userName, "Username cannot be empty");

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.USER_ID.equalIgnoreCase(userName))
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<OrderedTally<String>> getContributionLeaderBoard(int limit) {
        return makeOrderedTallyQuery(
                    dsl,
                    CHANGE_LOG,
                    CHANGE_LOG.USER_ID,
                    DSL.trueCondition())
                .orderBy(TALLY_COUNT_FIELD.desc())
                .limit(limit)
                .fetch(TO_ORDERED_STRING_TALLY);

    }


    public List<OrderedTally<String>> getContributionLeaderBoardLastMonth(int limit) {

        LocalDateTime monthStart = LocalDateTime.of(nowUtc().getYear(), nowUtc().getMonth(), 1, 0, 0);
        Condition condition = CHANGE_LOG.CREATED_AT.greaterThan(Timestamp.valueOf(monthStart));

        return makeOrderedTallyQuery(
                    dsl,
                    CHANGE_LOG,
                    CHANGE_LOG.USER_ID,
                    condition)
                .orderBy(TALLY_COUNT_FIELD.desc())
                .limit(limit)
                .fetch(TO_ORDERED_STRING_TALLY);
    }


    public List<OrderedTally<String>> getRankingOfContributors() {

        return makeOrderedTallyQuery(
                dsl,
                CHANGE_LOG,
                CHANGE_LOG.USER_ID,
                DSL.trueCondition())
                .fetch(TO_ORDERED_STRING_TALLY);
    }


    public List<Tally<String>> getContributionScoresForUsers(List<String> userIds) {
        return calculateStringTallies(
                dsl,
                CHANGE_LOG,
                CHANGE_LOG.USER_ID,
                CHANGE_LOG.USER_ID.in(userIds));
    }


    public int write(ChangeLog changeLog) {
        checkNotNull(changeLog, "changeLog must not be null");

        return dsl.insertInto(CHANGE_LOG)
                .set(CHANGE_LOG.MESSAGE, changeLog.message())
                .set(CHANGE_LOG.PARENT_ID, changeLog.parentReference().id())
                .set(CHANGE_LOG.PARENT_KIND, changeLog.parentReference().kind().name())
                .set(CHANGE_LOG.USER_ID, changeLog.userId())
                .set(CHANGE_LOG.SEVERITY, changeLog.severity().name())
                .set(CHANGE_LOG.CHILD_KIND, changeLog.childKind().map(ck -> ck.name()).orElse(null))
                .set(CHANGE_LOG.OPERATION, changeLog.operation().name())
                .set(CHANGE_LOG.CREATED_AT, Timestamp.valueOf(changeLog.createdAt()))
                .execute();
    }


    public int[] write(List<ChangeLog> changeLogs) {
        checkNotNull(changeLogs, "changeLogs must not be null");

        Query[] queries = changeLogs
                .stream()
                .map(changeLog -> DSL.insertInto(CHANGE_LOG)
                        .set(CHANGE_LOG.MESSAGE, changeLog.message())
                        .set(CHANGE_LOG.PARENT_ID, changeLog.parentReference().id())
                        .set(CHANGE_LOG.PARENT_KIND, changeLog.parentReference().kind().name())
                        .set(CHANGE_LOG.USER_ID, changeLog.userId())
                        .set(CHANGE_LOG.SEVERITY, changeLog.severity().name())
                        .set(CHANGE_LOG.CHILD_KIND, changeLog.childKind().map(ck -> ck.name()).orElse(null))
                        .set(CHANGE_LOG.OPERATION, changeLog.operation().name())
                        .set(CHANGE_LOG.CREATED_AT, Timestamp.valueOf(changeLog.createdAt())))
                .toArray(Query[]::new);
        return dsl.batch(queries).execute();
    }


}
