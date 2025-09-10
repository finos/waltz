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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.parser.Entity;
import java.sql.Date;
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

}
