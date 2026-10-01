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

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.tally.ChangeLogTally;
import org.finos.waltz.model.tally.DateTally;
import org.finos.waltz.model.tally.ImmutableChangeLogTally;
import org.finos.waltz.model.tally.ImmutableDateTally;
import org.finos.waltz.model.Duration;
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.RecordMapper;
import org.jooq.SelectHavingStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.DatePart;
import org.jooq.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.data.JooqUtilities.isoWeek;
import static org.finos.waltz.data.JooqUtilities.mkDateRangeCondition;
import static org.finos.waltz.model.EntityReference.mkRef;


@Repository
public class ChangeLogSummariesDao {

    private final DSLContext dsl;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            CHANGE_LOG.PARENT_ID,
            CHANGE_LOG.PARENT_KIND,
            newArrayList(EntityKind.APPLICATION))
            .as("entity_name");


    private static final RecordMapper<Record2<Date,Integer>, DateTally> TO_DATE_TALLY_MAPPER = record -> {
        Date date = record.value1();
        Integer count = record.value2();

        return ImmutableDateTally.builder()
                .date(date)
                .count(count)
                .build();
    };


    private static final RecordMapper<Record5<Long, String, String, String, Integer>, ChangeLogTally> TO_CHANGE_LOG_TALLY_MAPPER = record -> {

        EntityKind parentKind = EntityKind.valueOf(record.value2());
        EntityKind childKind = (record.value4() != null) ? EntityKind.valueOf(record.value4()) : null;
        Integer count = record.value5();

        EntityReference ref = mkRef(parentKind, record.value1(), record.value3());

        return ImmutableChangeLogTally.builder()
                .ref(ref)
                .childKind(childKind)
                .count(count)
                .build();
    };


    @Autowired
    public ChangeLogSummariesDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    /**
     * Returns the unaliased expressions that a query should group/order by to bucket rows into the
     * given frequency. Uses jOOQ's dedicated date functions (rather than {@code to_char} /
     * {@code date_trunc} / generic {@code extract}) so the queries stay dialect agnostic - these
     * translate to native equivalents on H2, Postgres and SQL Server. Group/order by must use the
     * unaliased expressions (SQL Server does not permit select-aliases in GROUP BY).
     */
    private List<Field<?>> periodGroupFields(Field<Timestamp> ts, Duration freq) {
        List<Field<?>> fields = new ArrayList<>();
        switch (freq) {
            case DAY:
                fields.add(DSL.date(ts));
                break;
            case WEEK:
                fields.add(DSL.year(ts));
                fields.add(isoWeek(dsl, ts));
                break;
            case YEAR:
                fields.add(DSL.year(ts));
                break;
            case MONTH:
            default:
                fields.add(DSL.year(ts));
                fields.add(DSL.month(ts));
        }
        return fields;
    }


    /**
     * The same expressions as {@link #periodGroupFields}, aliased for use in the SELECT clause so
     * the label can be reconstructed in Java by {@link #periodLabel}.
     */
    private List<Field<?>> periodSelectFields(Field<Timestamp> ts, Duration freq) {
        List<Field<?>> fields = new ArrayList<>();
        switch (freq) {
            case DAY:
                fields.add(DSL.date(ts).as("p_day"));
                break;
            case WEEK:
                fields.add(DSL.year(ts).as("p_year"));
                fields.add(isoWeek(dsl, ts).as("p_week"));
                break;
            case YEAR:
                fields.add(DSL.year(ts).as("p_year"));
                break;
            case MONTH:
            default:
                fields.add(DSL.year(ts).as("p_year"));
                fields.add(DSL.month(ts).as("p_month"));
        }
        return fields;
    }


    private static String periodLabel(Record r, Duration freq) {
        switch (freq) {
            case DAY:
                return r.get("p_day", Date.class).toLocalDate().toString();
            case WEEK:
                return String.format("%04d-W%02d", r.get("p_year", Integer.class), r.get("p_week", Integer.class));
            case YEAR:
                return String.format("%04d", r.get("p_year", Integer.class));
            case MONTH:
            default:
                return String.format("%04d-%02d", r.get("p_year", Integer.class), r.get("p_month", Integer.class));
        }
    }


    public List<DateTally> findCountByDateForParentKindBySelector(GenericSelector selector,
                                                                  Optional<Integer> limit) {
        checkNotNull(selector, "selector must not be null");

        Field<Date> date = DSL.date(CHANGE_LOG.CREATED_AT);

        return dsl
                .select(date, DSL.count(CHANGE_LOG.ID))
                .from(CHANGE_LOG)
                .where(CHANGE_LOG.PARENT_ID.in(selector.selector())
                .and(CHANGE_LOG.PARENT_KIND.eq(selector.kind().name())))
                .groupBy(date)
                .orderBy(date.desc())
                .limit(limit.orElse(365))
                .fetch(TO_DATE_TALLY_MAPPER);
    }


    public List<ChangeLogTally> findCountByParentAndChildKindForDateRangeBySelector(GenericSelector genericSelector,
                                                                                    Date startDate,
                                                                                    Date endDate,
                                                                                    Optional<Integer> limit) {
        checkNotNull(genericSelector, "genericSelector must not be null");

        AggregateFunction<Integer> count = DSL.count(CHANGE_LOG.ID);
        Condition dateRangeCondition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.PARENT_ID,
                        CHANGE_LOG.PARENT_KIND,
                        ENTITY_NAME_FIELD,
                        CHANGE_LOG.CHILD_KIND,
                        count)
                .from(CHANGE_LOG)
                .where(dsl
                        .renderInlined(CHANGE_LOG.PARENT_ID.in(genericSelector.selector())
                                .and(CHANGE_LOG.PARENT_KIND.eq(genericSelector.kind().name()))
                                .and(dateRangeCondition)))
                .groupBy(CHANGE_LOG.PARENT_ID, CHANGE_LOG.PARENT_KIND, CHANGE_LOG.CHILD_KIND)
                .orderBy(count.desc())
                .limit(limit.orElse(Integer.MAX_VALUE))
                .fetch(TO_CHANGE_LOG_TALLY_MAPPER);
    }

    public Map<Integer, Long> findYearOnYearChanges(EntityKind parentEntityKind, EntityKind childEntityKind) {
        Condition parentEntityKindSelector = parentEntityKind == null ? DSL.trueCondition()
                : CHANGE_LOG.PARENT_KIND.eq(parentEntityKind.name());

        Condition childEntityKindSelector = childEntityKind == null ? DSL.trueCondition()
                : CHANGE_LOG.CHILD_KIND.eq(childEntityKind.name());

        Field<Integer> yearField = DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.YEAR);

        SelectHavingStep<Record2<Integer, Integer>> qry = dsl
                .select(DSL.count(CHANGE_LOG.ID).as("counts"), yearField.as("year"))
                .from(CHANGE_LOG)
                .where(parentEntityKindSelector.and(childEntityKindSelector))
                .groupBy(yearField);

        return  qry
                .fetchMap(r -> r.get("year", Integer.class),
                        r -> r.get("counts", Long.class));
    }

    public List<String> findChangeLogParentEntities() {
        SelectJoinStep<Record1<String>> parentKindSelector = dsl
                .selectDistinct(CHANGE_LOG.PARENT_KIND)
                .from(CHANGE_LOG);

        return parentKindSelector
                .fetchInto(String.class);
    }

    public List<Integer> findChangeLogYears() {
        return dsl
                .selectDistinct(DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.YEAR).as("year"))
                .from(CHANGE_LOG)
                .fetch(r -> r.get("year", Integer.class));
    }

    public Map<Integer, Long> findMonthOnMonthChanges(EntityKind parentEntityKind, EntityKind childEntityKind, Integer currentYear) {
        Condition parentEntityKindSelector = parentEntityKind == null ? DSL.trueCondition()
                : CHANGE_LOG.PARENT_KIND.eq(parentEntityKind.name());

        Condition childEntityKindSelector = childEntityKind == null ? DSL.trueCondition()
                : CHANGE_LOG.CHILD_KIND.eq(childEntityKind.name());

        Field<Integer> monthField = DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.MONTH);

        SelectHavingStep<Record2<Integer, Integer>> qry = dsl
                .select(DSL.count(CHANGE_LOG.ID).as("counts"), monthField.as("month"))
                .from(CHANGE_LOG)
                .where(parentEntityKindSelector
                        .and(childEntityKindSelector)
                        .and(DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.YEAR).eq(currentYear)))
                .groupBy(monthField);

        return  qry
                .fetchMap(r -> r.get("month", Integer.class),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get changes for given period
     */
    public Map<String, Map<String, Long>> findChangesByPeriod(EntityKind parentEntityKind,
                                                 EntityKind childEntityKind,
                                                 LocalDate startDate,
                                                 LocalDate endDate,
                                                 Duration freq) {

        Condition parentEntityKindSelector = parentEntityKind == null
                ? DSL.trueCondition()
                : CHANGE_LOG.PARENT_KIND.eq(parentEntityKind.name());

        Condition childEntityKindSelector = childEntityKind == null
                ? DSL.trueCondition()
                : CHANGE_LOG.CHILD_KIND.eq(childEntityKind.name());

        Condition dateRangeSelector = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        List<Field<?>> periodGroup = periodGroupFields(CHANGE_LOG.CREATED_AT, freq);
        List<Field<?>> selectFields = new ArrayList<>(periodSelectFields(CHANGE_LOG.CREATED_AT, freq));
        selectFields.add(DSL.count(CHANGE_LOG.ID).as("counts"));
        selectFields.add(DSL.countDistinct(CHANGE_LOG.USER_ID).as("distinct_user_count"));

        Result<Record> result = dsl
                .select(selectFields)
                .from(CHANGE_LOG)
                .where(parentEntityKindSelector
                        .and(childEntityKindSelector)
                        .and(dateRangeSelector))
                .groupBy(periodGroup)
                .orderBy(periodGroup)
                .fetch();

        Map<String, Map<String, Long>> resultMap = new HashMap<>();
        for (Record record : result) {
            Map<String, Long> values = new HashMap<>();
            values.put("counts", record.get("counts", Long.class));
            values.put("distinctUserCount", record.get("distinct_user_count", Long.class));

            resultMap.put(periodLabel(record, freq), values);
        }

        return resultMap;
    }

    /**
     * Get change activity by severity level
     */
    public Map<String, Long> findChangesBySeverity(LocalDate startDate, LocalDate endDate) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.SEVERITY, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.SEVERITY.isNotNull()))
                .groupBy(CHANGE_LOG.SEVERITY)
                .fetchMap(r -> r.get(CHANGE_LOG.SEVERITY),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get change activity by entity kind (parent)
     */
    public Map<String, Long> findChangesByEntityKind(LocalDate startDate, LocalDate endDate, int limit) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.PARENT_KIND, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(CHANGE_LOG.PARENT_KIND)
                .orderBy(DSL.count().desc())
                .limit(limit)
                .fetchMap(r -> r.get(CHANGE_LOG.PARENT_KIND),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get top contributors by change count
     */
    public Map<String, Long> findTopContributors(LocalDate startDate, LocalDate endDate, int limit) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.USER_ID, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(CHANGE_LOG.USER_ID)
                .orderBy(DSL.count().desc())
                .limit(limit)
                .fetchMap(r -> r.get(CHANGE_LOG.USER_ID),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get top contributors with period-based aggregation
     */
    public Map<String, Map<String, Long>> findTopContributorsByPeriod(LocalDate startDate, LocalDate endDate, Duration freq, int limit) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        // First, get top contributors overall
        List<String> topUsers = dsl
                .select(CHANGE_LOG.USER_ID)
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(CHANGE_LOG.USER_ID)
                .orderBy(DSL.count().desc())
                .limit(limit)
                .fetch(r -> r.get(CHANGE_LOG.USER_ID));

        List<Field<?>> periodGroup = periodGroupFields(CHANGE_LOG.CREATED_AT, freq);
        List<Field<?>> selectFields = new ArrayList<>(periodSelectFields(CHANGE_LOG.CREATED_AT, freq));
        selectFields.add(0, CHANGE_LOG.USER_ID);
        selectFields.add(DSL.count().as("counts"));
        List<Field<?>> groupFields = new ArrayList<>(periodGroup);
        groupFields.add(CHANGE_LOG.USER_ID);

        // Then get their activity by period
        Result<Record> result = dsl
                .select(selectFields)
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.USER_ID.in(topUsers)))
                .groupBy(groupFields)
                .orderBy(periodGroup)
                .fetch();

        Map<String, Map<String, Long>> trends = new HashMap<>();
        for (Record record : result) {
            String userId = record.get(CHANGE_LOG.USER_ID);
            Long count = record.get("counts", Long.class);

            trends.computeIfAbsent(userId, k -> new HashMap<>()).put(periodLabel(record, freq), count);
        }

        return trends;
    }

    /**
     * Get change activity by day of week
     */
    public Map<Integer, Long> findChangesByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        // Bucket by calendar day in the query (portable) then fold into ISO day-of-week (1=Mon..7=Sun)
        // in Java, avoiding dialect specific weekday extraction.
        Field<Date> dayField = DSL.date(CHANGE_LOG.CREATED_AT);

        Map<Integer, Long> countsByDayOfWeek = new TreeMap<>();
        dsl
                .select(dayField.as("day"), DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate))
                .groupBy(dayField)
                .fetch()
                .forEach(r -> {
                    int dayOfWeek = r.get("day", Date.class).toLocalDate().getDayOfWeek().getValue();
                    countsByDayOfWeek.merge(dayOfWeek, r.get("counts", Long.class), Long::sum);
                });

        return countsByDayOfWeek;
    }

    /**
     * Get change activity by operation type (ADD/UPDATE/REMOVE)
     */
    public Map<String, Long> findChangesByOperation(LocalDate startDate, LocalDate endDate) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.OPERATION, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.OPERATION.isNotNull()))
                .groupBy(CHANGE_LOG.OPERATION)
                .fetchMap(r -> r.get(CHANGE_LOG.OPERATION),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get change activity by child entity kind
     */
    public Map<String, Long> findChangesByChildKind(LocalDate startDate, LocalDate endDate, int limit) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        return dsl
                .select(CHANGE_LOG.CHILD_KIND, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.CHILD_KIND.isNotNull()))
                .groupBy(CHANGE_LOG.CHILD_KIND)
                .orderBy(DSL.count().desc())
                .limit(limit)
                .fetchMap(r -> r.get(CHANGE_LOG.CHILD_KIND),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get operation trends over time
     */
    public Map<String, Map<String, Long>> findOperationTrends(LocalDate startDate, LocalDate endDate, Duration freq) {
        Condition condition = mkDateRangeCondition(CHANGE_LOG.CREATED_AT, startDate, endDate);

        List<Field<?>> periodGroup = periodGroupFields(CHANGE_LOG.CREATED_AT, freq);
        List<Field<?>> selectFields = new ArrayList<>(periodSelectFields(CHANGE_LOG.CREATED_AT, freq));
        selectFields.add(CHANGE_LOG.OPERATION);
        selectFields.add(DSL.count().as("counts"));
        List<Field<?>> groupFields = new ArrayList<>(periodGroup);
        groupFields.add(CHANGE_LOG.OPERATION);

        Result<Record> result = dsl
                .select(selectFields)
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.OPERATION.isNotNull()))
                .groupBy(groupFields)
                .orderBy(periodGroup)
                .fetch();

        Map<String, Map<String, Long>> trends = new HashMap<>();
        for (Record record : result) {
            String operation = record.get(CHANGE_LOG.OPERATION);
            Long count = record.get("counts", Long.class);

            trends.computeIfAbsent(operation, k -> new HashMap<>()).put(periodLabel(record, freq), count);
        }

        return trends;
    }
}
