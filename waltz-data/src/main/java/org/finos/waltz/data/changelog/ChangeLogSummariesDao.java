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
import org.jooq.AggregateFunction;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.RecordMapper;
import org.jooq.SelectHavingStep;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.DatePart;
import org.jooq.Result;
import org.jooq.Record3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.parser.Entity;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.finos.waltz.schema.tables.AccessLog.ACCESS_LOG;
import static org.finos.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.newArrayList;
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
                                                 String freq) {

        Condition parentEntityKindSelector = parentEntityKind == null
                ? DSL.trueCondition()
                : CHANGE_LOG.PARENT_KIND.eq(parentEntityKind.name());

        Condition childEntityKindSelector = childEntityKind == null
                ? DSL.trueCondition()
                : CHANGE_LOG.CHILD_KIND.eq(childEntityKind.name());

        Condition dateRangeSelector = DSL.trueCondition();

        if (startDate != null) {
            dateRangeSelector = dateRangeSelector.and(
                    CHANGE_LOG.CREATED_AT.ge(
                            Timestamp.valueOf(startDate.atStartOfDay()) // 00:00:00 start of day
                    )
            );
        }
        if (endDate != null) {
            dateRangeSelector = dateRangeSelector.and(
                    CHANGE_LOG.CREATED_AT.le(
                            Timestamp.valueOf(endDate.atTime(LocalTime.MAX)) // 23:59:59 end of day
                    )
            );
        }

        Field<String> periodField;
        switch (freq.toLowerCase()) {
            case "day":
                periodField = DSL.field("to_char({0}, 'YYYY-MM-DD')",
                        String.class, CHANGE_LOG.CREATED_AT).as("period");
                break;
            case "week":
                periodField = DSL.field("to_char(date_trunc('week', {0}), 'YYYY-MM-DD')",
                        String.class, CHANGE_LOG.CREATED_AT).as("period");
                break;
            case "year":
                periodField = DSL.field("to_char({0}, 'YYYY')",
                        String.class, CHANGE_LOG.CREATED_AT).as("period");
                break;
            case "month":
            default:
                periodField = DSL.field("to_char({0}, 'YYYY-MM')",
                        String.class, CHANGE_LOG.CREATED_AT).as("period");
        }



        Result<Record3<String, Integer, Integer>> result = dsl
                .select(periodField.as("period"), 
                        DSL.count(CHANGE_LOG.ID).as("counts"),
                        DSL.countDistinct(CHANGE_LOG.USER_ID).as("distinct_user_count"))
                .from(CHANGE_LOG)
                .where(parentEntityKindSelector
                        .and(childEntityKindSelector)
                        .and(dateRangeSelector))
                .groupBy(periodField)
                .orderBy(periodField)
                .fetch();

        Map<String, Map<String, Long>> resultMap = new java.util.HashMap<>();
        for (Record3<String, Integer, Integer> record : result) {
            String period_key = record.get("period", String.class);
            Long counts = record.get("counts", Long.class);
            Long distinctUserCount = record.get("distinct_user_count", Long.class);
            
            Map<String, Long> values = new java.util.HashMap<>();
            values.put("counts", counts);
            values.put("distinctUserCount", distinctUserCount);
            
            resultMap.put(period_key, values);
        }
        
        return resultMap;
    }

    /**
     * Get change activity by severity level
     */
    public Map<String, Long> findChangesBySeverity(String startDate, String endDate) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

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
    public Map<String, Long> findChangesByEntityKind(String startDate, String endDate, int limit) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

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
    public Map<String, Long> findTopContributors(String startDate, String endDate, int limit) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

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
    public Map<String, Map<String, Long>> findTopContributorsByPeriod(String startDate, String endDate, String freq, int limit) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

        Field<String> periodField;
        switch (freq.toLowerCase()) {
            case "day":
                periodField = DSL.field("to_char({0}, 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "week":
                periodField = DSL.field("to_char(date_trunc('week', {0}), 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "year":
                periodField = DSL.field("to_char({0}, 'YYYY')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "month":
            default:
                periodField = DSL.field("to_char({0}, 'YYYY-MM')", String.class, CHANGE_LOG.CREATED_AT);
        }

        // First, get top contributors overall
        List<String> topUsers = dsl
                .select(CHANGE_LOG.USER_ID)
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(CHANGE_LOG.USER_ID)
                .orderBy(DSL.count().desc())
                .limit(limit)
                .fetch(r -> r.get(CHANGE_LOG.USER_ID));

        // Then get their activity by period
        Result<Record3<String, String, Integer>> result = dsl
                .select(CHANGE_LOG.USER_ID, periodField.as("period"), DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.USER_ID.in(topUsers)))
                .groupBy(CHANGE_LOG.USER_ID, periodField)
                .orderBy(periodField)
                .fetch();

        Map<String, Map<String, Long>> trends = new HashMap<>();
        for (Record3<String, String, Integer> record : result) {
            String userId = record.get(CHANGE_LOG.USER_ID);
            String periodValue = record.get("period", String.class);
            Long count = record.get("counts", Integer.class).longValue();
            
            trends.computeIfAbsent(userId, k -> new HashMap<>()).put(periodValue, count);
        }
        
        return trends;
    }

    /**
     * Get change activity by hour of day
     */
    public Map<Integer, Long> findChangesByHourOfDay(String startDate, String endDate) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

        Field<Integer> hourField = DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.HOUR);
        
        return dsl
                .select(hourField.as("hour"), DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(hourField)
                .orderBy(hourField)
                .fetchMap(r -> r.get("hour", Integer.class),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get change activity by day of week
     */
    public Map<Integer, Long> findChangesByDayOfWeek(String startDate, String endDate) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

        // Use a more compatible approach for day of week extraction
        Field<Integer> dayOfWeekField;
        try {
            // Try PostgreSQL/standard SQL approach first
            dayOfWeekField = DSL.field(
                "CASE " +
                "WHEN EXTRACT(DOW FROM {0}) = 0 THEN 7 " +
                "ELSE EXTRACT(DOW FROM {0}) " +
                "END", Integer.class, CHANGE_LOG.CREATED_AT
            );
        } catch (Exception e) {
            // Fallback to basic JOOQ approach
            dayOfWeekField = DSL.extract(CHANGE_LOG.CREATED_AT, DatePart.DAY_OF_WEEK);
        }
        
        return dsl
                .select(dayOfWeekField.as("day_of_week"), DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition)
                .groupBy(dayOfWeekField)
                .orderBy(dayOfWeekField)
                .fetchMap(r -> r.get("day_of_week", Integer.class),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get change activity by operation type (ADD/UPDATE/REMOVE)
     */
    public Map<String, Long> findChangesByOperation(String startDate, String endDate) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }
        
        return dsl
                .select(CHANGE_LOG.OPERATION, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.OPERATION.isNotNull()))
                .groupBy(CHANGE_LOG.OPERATION)
                .fetchMap(r -> r.get(CHANGE_LOG.OPERATION),
                        r -> r.get("counts", Long.class));
    }

    /**
     * Get changes by operation with period-based aggregation
     */
    public Map<String, Map<String, Long>> findChangesByOperationByPeriod(String startDate, String endDate, String freq) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

        Field<String> periodField;
        switch (freq.toLowerCase()) {
            case "day":
                periodField = DSL.field("to_char({0}, 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "week":
                periodField = DSL.field("to_char(date_trunc('week', {0}), 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "year":
                periodField = DSL.field("to_char({0}, 'YYYY')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "month":
            default:
                periodField = DSL.field("to_char({0}, 'YYYY-MM')", String.class, CHANGE_LOG.CREATED_AT);
        }
        
        Result<Record3<String, String, Integer>> result = dsl
                .select(CHANGE_LOG.OPERATION, periodField.as("period"), DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.OPERATION.isNotNull()))
                .groupBy(CHANGE_LOG.OPERATION, periodField)
                .orderBy(periodField)
                .fetch();

        Map<String, Map<String, Long>> trends = new HashMap<>();
        for (Record3<String, String, Integer> record : result) {
            String operation = record.get(CHANGE_LOG.OPERATION);
            String periodValue = record.get("period", String.class);
            Long count = record.get("counts", Integer.class).longValue();
            
            trends.computeIfAbsent(operation, k -> new HashMap<>()).put(periodValue, count);
        }
        
        return trends;
    }

    /**
     * Get change activity by child entity kind
     */
    public Map<String, Long> findChangesByChildKind(String startDate, String endDate, int limit) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }
        
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
    public Map<String, Map<String, Long>> findOperationTrends(String startDate, String endDate, String freq) {
        Condition condition = DSL.trueCondition();
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            condition = condition.and(CHANGE_LOG.CREATED_AT.between(
                    Timestamp.valueOf(start.atStartOfDay()),
                    Timestamp.valueOf(end.atTime(23, 59, 59))
            ));
        }

        Field<String> periodField;
        switch (freq.toLowerCase()) {
            case "day":
                periodField = DSL.field("to_char({0}, 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "week":
                periodField = DSL.field("to_char(date_trunc('week', {0}), 'YYYY-MM-DD')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "year":
                periodField = DSL.field("to_char({0}, 'YYYY')", String.class, CHANGE_LOG.CREATED_AT);
                break;
            case "month":
            default:
                periodField = DSL.field("to_char({0}, 'YYYY-MM')", String.class, CHANGE_LOG.CREATED_AT);
        }
        
        Result<Record3<String, String, Integer>> result = dsl
                .select(periodField.as("period"), CHANGE_LOG.OPERATION, DSL.count().as("counts"))
                .from(CHANGE_LOG)
                .where(condition.and(CHANGE_LOG.OPERATION.isNotNull()))
                .groupBy(periodField, CHANGE_LOG.OPERATION)
                .orderBy(periodField)
                .fetch();

        Map<String, Map<String, Long>> trends = new HashMap<>();
        for (Record3<String, String, Integer> record : result) {
            String periodValue = record.get("period", String.class);
            String operation = record.get(CHANGE_LOG.OPERATION);
            Long count = record.get("counts", Integer.class).longValue();
            
            trends.computeIfAbsent(operation, k -> new HashMap<>()).put(periodValue, count);
        }
        
        return trends;
    }
}
