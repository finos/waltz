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

package org.finos.waltz.data.changelog;

import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.tally.OrderedTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.tables.AttestationInstance;
import org.finos.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.nowUtc;
import static org.finos.waltz.common.ObjectUtilities.firstNotNull;
import static org.finos.waltz.data.JooqUtilities.*;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG;


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
                .childKind(Optional.ofNullable(record.getChildKind()).map(EntityKind::valueOf))
                .operation(Operation.valueOf(record.getOperation()))
                .createdAt(record.getCreatedAt().toLocalDateTime())
                .build();
    };


    public static SelectConditionStep<Record> mkUnattestedChangesQuery(EntityReference ref) {
        org.finos.waltz.schema.tables.ChangeLog cl = org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG.as("cl");
        AttestationInstance ai = AttestationInstance.ATTESTATION_INSTANCE.as("ai");

        Condition changeLogEntryOfInterest = cl.OPERATION.in(
                Operation.UPDATE.name(),
                Operation.ADD.name(),
                Operation.REMOVE.name());

        // this query works by first finding the latest attestations for the given entity
        Table<Record4<String, Long, String, Timestamp>> latestAttestationsSubQuery = DSL
                .select(ai.PARENT_ENTITY_KIND.as("pek"),
                        ai.PARENT_ENTITY_ID .as("pei"),
                        ai.ATTESTED_ENTITY_KIND.as("aek"),
                        DSL.max(ai.ATTESTED_AT).as("aat"))
                .from(ai)
                .where(ai.PARENT_ENTITY_ID.eq(ref.id()))
                .and(ai.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ai.ATTESTED_AT.isNotNull())
                .groupBy(ai.PARENT_ENTITY_KIND, ai.PARENT_ENTITY_ID, ai.ATTESTED_ENTITY_KIND)
                .asTable("latest_attestation");

        // and joining that query to the changelog (for that entity)
        Condition joinChangeLogToLatestAttestationCondition = latestAttestationsSubQuery.field("pek", String.class).eq(cl.PARENT_KIND)
                .and(latestAttestationsSubQuery.field("pei", Long.class).eq(cl.PARENT_ID))
                .and(latestAttestationsSubQuery.field("aek", String.class).eq(cl.CHILD_KIND));

        // giving the final query which limits the changelog based to those entries after the latest attestation:
        return DSL
                .select(cl.fields())
                .from(cl)
                .innerJoin(latestAttestationsSubQuery)
                .on(joinChangeLogToLatestAttestationCondition)
                .where(cl.CREATED_AT.greaterThan(latestAttestationsSubQuery.field("aat", Timestamp.class)))
                .and(changeLogEntryOfInterest);
    }


    @Autowired
    public ChangeLogDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ChangeLog> findByParentReference(EntityReference ref,
                                                 Optional<java.util.Date> date,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        Condition dateCondition = date
                .map(d -> mkDateRangeCondition(CHANGE_LOG.CREATED_AT, d))
                .orElse(DSL.trueCondition());

        Integer limitValue = limit.orElse(Integer.MAX_VALUE);

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()))
                .and(dateCondition)
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .limit(limitValue)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeLog> findByPersonReference(EntityReference ref,
                                                 Optional<java.util.Date> date,
                                                 Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        Condition dateCondition = date
                .map(d -> mkDateRangeCondition(CHANGE_LOG.CREATED_AT, d))
                .orElse(DSL.trueCondition());

        Integer limitValue = limit.orElse(Integer.MAX_VALUE);

        SelectConditionStep<Record> byParentRef = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(dateCondition)
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()));

        SelectConditionStep<Record> byUserId = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .innerJoin(PERSON).on(PERSON.EMAIL.eq(CHANGE_LOG.USER_ID))
                .where(PERSON.ID.eq(ref.id()))
                .and(dateCondition);

        SelectOrderByStep<Record> union = byParentRef.unionAll(byUserId);

        return dsl
                .select(union.fields())
                .from(union.asTable())
                .orderBy(union.field("created_at").desc())
                .limit(limitValue)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeLog> findByParentReferenceForDateRange(EntityReference ref,
                                                             Date startDate,
                                                             Date endDate,
                                                             Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        Condition dateCondition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        Integer limitValue = limit.orElse(Integer.MAX_VALUE);

        return dsl.select()
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()))
                .and(dateCondition)
                .orderBy(CHANGE_LOG.CREATED_AT.desc())
                .limit(limitValue)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeLog> findByPersonReferenceForDateRange(EntityReference ref,
                                                             Date startDate,
                                                             Date endDate,
                                                             Optional<Integer> limit) {
        checkNotNull(ref, "ref must not be null");

        Condition dateCondition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        Integer limitValue = limit.orElse(Integer.MAX_VALUE);

        SelectConditionStep<Record> byParentRef = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.eq(ref.id()))
                .and(dateCondition)
                .and(CHANGE_LOG.PARENT_KIND.eq(ref.kind().name()));

        SelectConditionStep<Record> byUserId = DSL
                .select(CHANGE_LOG.fields())
                .from(CHANGE_LOG)
                .innerJoin(PERSON).on(PERSON.EMAIL.eq(CHANGE_LOG.USER_ID))
                .where(PERSON.ID.eq(ref.id()))
                .and(dateCondition);

        SelectOrderByStep<Record> union = byParentRef.unionAll(byUserId);

        return dsl
                .select(union.fields())
                .from(union.asTable())
                .orderBy(union.field("created_at").desc())
                .limit(limitValue)
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


    public int[] write(DSLContext tx, Collection<ChangeLog> changeLogs) {
        checkNotNull(changeLogs, "changeLogs must not be null");

        DSLContext dslContext = firstNotNull(tx, dsl);

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
        return dslContext.batch(queries).execute();
    }


    /**
     * Given an entity ref this function will determine all changelog entries made _after_ the latest
     * attestations for that entity.  Change log is matched between the attestation kind and the change
     * log child kind.
     *
     * @param ref
     * @return list of changes (empty if no attestations or if no changes)
     */
    public List<ChangeLog> findUnattestedChanges(EntityReference ref) {

        SelectConditionStep<Record> qry = mkUnattestedChangesQuery(ref);

        return dsl
                .selectQuery(qry)
                .fetch(TO_DOMAIN_MAPPER);
    }

}
