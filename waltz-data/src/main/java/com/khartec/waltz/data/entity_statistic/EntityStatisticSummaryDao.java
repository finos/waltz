/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.Duration;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.ImmutableTallyPack;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.model.tally.TallyPack;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class EntityStatisticSummaryDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    private static final Field<BigDecimal> avgTotal = DSL.avg(cast(esv.VALUE, Long.class)).as("total");
    private static final Field<BigDecimal> sumTotal = DSL.sum(cast(esv.VALUE, Long.class)).as("total");
    private static final Field<Integer> countTotal = DSL.count();
    private static final Function<BigDecimal, Double> toBigDecimalTally = v -> v.doubleValue();
    private static final Function<Integer, Double> toIntegerTally = v -> v.doubleValue();
    private static final Field<Timestamp> maxCreatedAtField = DSL.field("max_created_at", Timestamp.class);
    private static final Field<Date> castDateField = cast(esv.CREATED_AT, Date.class);
    private static final Field<java.sql.Date> esvCreatedAtDateOnly = castDateField.as("esv_created_at_date_only");

    private final DSLContext dsl;
    private final DBExecutorPoolInterface dbExecutorPool;


    @Autowired
    public EntityStatisticSummaryDao(DSLContext dsl, DBExecutorPoolInterface dbExecutorPool) {
        checkNotNull(dsl, "dsl cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        this.dsl = dsl;
        this.dbExecutorPool = dbExecutorPool;
    }


    public TallyPack<String> generateWithAvgByValue(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, avgTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateWithAvgByValue(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, avgTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateHistoricWithAvgByValue(Long statisticId,
                                                                  Select<Record1<Long>> appIdSelector,
                                                                  Duration duration) {
        return generateHistoricSummary(statisticId, appIdSelector, avgTotal, toBigDecimalTally, duration);
    }


    public TallyPack<String> generateWithSumByValue(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, sumTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateWithSumByValue(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, sumTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateHistoricWithSumByValue(Long statisticId,
                                                                  Select<Record1<Long>> appIdSelector,
                                                                  Duration duration) {
        return generateHistoricSummary(statisticId, appIdSelector, sumTotal, toBigDecimalTally, duration);
    }


    public TallyPack<String> generateWithCountByEntity(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, countTotal, toIntegerTally);
    }


    public List<TallyPack<String>> generateWithCountByEntity(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, countTotal, toIntegerTally);
    }


    public List<TallyPack<String>> generateHistoricWithCountByEntity(Long statisticId,
                                                                     Select<Record1<Long>> appIdSelector,
                                                                     Duration duration) {
        return generateHistoricSummary(statisticId, appIdSelector, countTotal, toIntegerTally, duration);
    }


    public List<TallyPack<String>> generateWithNoRollup(Collection<Long> statisticIds,
                                                        EntityReference entityReference) {

        checkNotNull(statisticIds, "statisticIds cannot be null");
        checkNotNull(entityReference, "entityReference cannot be null");

        if (statisticIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Future<TallyPack<String>>> summaryFutures = statisticIds.stream()
                .map(statId -> dbExecutorPool.submit(() ->
                        generateWithNoRollup(statId, entityReference)))
                .collect(toList());

        return summaryFutures.stream()
                .map(f -> Unchecked.supplier(() -> f.get())
                        .get())
                .collect(toList());
    }


    public TallyPack<String> generateWithNoRollup(Long statisticId, EntityReference entityReference) {
        Condition condition = mkNoRollupCondition(
                newArrayList(statisticId),
                entityReference,
                esv.CURRENT.eq(true));

        Result<Record4<Long, String, String, Timestamp>> values = dsl
                .select(esv.STATISTIC_ID, esv.OUTCOME, esv.VALUE, max(esv.CREATED_AT).as(maxCreatedAtField))
                .from(esv)
                .where(dsl.renderInlined(condition))
                .groupBy(esv.STATISTIC_ID, esv.OUTCOME, esv.VALUE)
                .fetch();

        LocalDateTime maxCreatedAt = values.isNotEmpty()
                                    ? values.get(0).getValue(maxCreatedAtField).toLocalDateTime()
                                    : nowUtc();

        List<Tally<String>> tallies = values.stream()
                .map(r -> ImmutableTally.<String>builder()
                        .count(Double.parseDouble(r.getValue(esv.VALUE)))
                        .id(r.getValue(esv.OUTCOME))
                        .build())
                .collect(toList());

        return ImmutableTallyPack.<String>builder()
                .entityReference(EntityReference.mkRef(EntityKind.ENTITY_STATISTIC, statisticId))
                .tallies(tallies)
                .lastUpdatedAt(maxCreatedAt)
                .build();
    }


    public List<TallyPack<String>> generateHistoricWithNoRollup(Long statisticId,
                                                                EntityReference entityReference,
                                                                Duration duration) {

        checkNotNull(statisticId, "statisticId cannot be null");
        checkNotNull(entityReference, "entityReference cannot be null");
        checkNotNull(duration, "duration cannot be null");

        Condition condition = mkNoRollupCondition(
                newArrayList(statisticId),
                entityReference,
                esv.CURRENT.eq(false));

        Result<Record4<java.sql.Date, Long, String, String>> values = dsl
                .select(esvCreatedAtDateOnly, esv.STATISTIC_ID, esv.OUTCOME, esv.VALUE)
                .from(esv)
                .where(dsl.renderInlined(condition))
                .and(dsl.renderInlined(mkHistoryDurationCondition(duration)))
                .groupBy(castDateField, esv.STATISTIC_ID, esv.OUTCOME, esv.VALUE)
                .orderBy(esvCreatedAtDateOnly.asc())
                .fetch();

        return values
                .stream()
                .map(r -> tuple(r.getValue(esvCreatedAtDateOnly),
                                ImmutableTally.<String>builder()
                                        .count(Double.parseDouble(r.getValue(esv.VALUE)))
                                        .id(r.getValue(esv.OUTCOME))
                                        .build()))
                .collect(groupingBy(t -> t.v1().toLocalDate(),
                             mapping(t -> t.v2(),
                                     toList())))
                .entrySet()
                .stream()
                .map(e -> ImmutableTallyPack.<String>builder()
                        .entityReference(EntityReference.mkRef(EntityKind.ENTITY_STATISTIC, statisticId))
                        .tallies(e.getValue())
                        .lastUpdatedAt(e.getKey().atStartOfDay())
                        .build())
                .collect(toList());
    }


    private <T> TallyPack<String> generateSummary(Long statisticId,
                                                  Select<Record1<Long>> appIdSelector,
                                                  Field<T> aggregateField,
                                                  Function<T, Double> toTally) {

        Condition condition = mkSummaryCondition(
                newArrayList(statisticId),
                appIdSelector,
                esv.CURRENT.eq(true));

        Result<Record3<String, T, Timestamp>> values = dsl
                .select(esv.OUTCOME, aggregateField, max(esv.CREATED_AT).as(maxCreatedAtField))
                .from(esv)
                .where(condition)
                .groupBy(esv.OUTCOME)
                .fetch();

        LocalDateTime maxCreatedAt = values.isNotEmpty()
                                        ? values.get(0).getValue(maxCreatedAtField).toLocalDateTime()
                                        : nowUtc();

        List<Tally<String>> tallies = values
                .stream()
                .map(r -> ImmutableTally.<String>builder()
                        .count(toTally.apply(r.getValue(aggregateField)))
                        .id(r.getValue(esv.OUTCOME))
                        .build())
                .collect(toList());

        return ImmutableTallyPack.<String>builder()
                .entityReference(EntityReference.mkRef(EntityKind.ENTITY_STATISTIC, statisticId))
                .tallies(tallies)
                .lastUpdatedAt(maxCreatedAt)
                .build();
    }


    private <T> List<TallyPack<String>> generateSummaries(Collection<Long> statisticIds,
                                                          Select<Record1<Long>> appIdSelector,
                                                          Field<T> aggregateField,
                                                          Function<T, Double> toTally) {

        checkNotNull(statisticIds, "statisticIds cannot be null");
        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(aggregateField, "aggregateField cannot be null");
        checkNotNull(toTally, "toTally function cannot be null");

        if (statisticIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Future<TallyPack<String>>> summaryFutures = statisticIds.stream()
                .map(statId -> dbExecutorPool.submit(() ->
                        generateSummary(statId, appIdSelector, aggregateField, toTally)))
                .collect(toList());

        return summaryFutures.stream()
                .map(f -> Unchecked.supplier(() -> f.get())
                        .get())
                .collect(toList());
    }


    private <T> List<TallyPack<String>> generateHistoricSummary(Long statisticId,
                                                                Select<Record1<Long>> appIdSelector,
                                                                Field<T> aggregateField,
                                                                Function<T, Double> toTally,
                                                                Duration duration) {

        checkNotNull(statisticId, "statisticId cannot be null");
        checkNotNull(appIdSelector, "appIdSelector cannot be null");
        checkNotNull(aggregateField, "aggregateField cannot be null");
        checkNotNull(toTally, "toTally function cannot be null");
        checkNotNull(duration, "duration cannot be null");

        Condition condition = mkSummaryCondition(
                newArrayList(statisticId),
                appIdSelector,
                esv.CURRENT.eq(false));

        Result<Record3<Date, String, T>> values = dsl
                .select(esvCreatedAtDateOnly, esv.OUTCOME, aggregateField)
                .from(esv)
                .where(dsl.renderInlined(condition))
                .and(dsl.renderInlined(mkHistoryDurationCondition(duration)))
                .groupBy(castDateField, esv.OUTCOME)
                .orderBy(esvCreatedAtDateOnly.asc())
                .fetch();

        return values
                .stream()
                .map(r -> tuple(r.getValue(esvCreatedAtDateOnly),
                                ImmutableTally.<String>builder()
                                        .count(toTally.apply(r.getValue(aggregateField)))
                                        .id(r.getValue(esv.OUTCOME))
                                        .build()))
                .collect(groupingBy(t -> t.v1().toLocalDate(),
                             mapping(t -> t.v2(),
                                     toList())))
                .entrySet()
                .stream()
                .map(e -> ImmutableTallyPack.<String>builder()
                        .entityReference(EntityReference.mkRef(EntityKind.ENTITY_STATISTIC, statisticId))
                        .tallies(e.getValue())
                        .lastUpdatedAt(e.getKey().atStartOfDay())
                        .build())
                .collect(toList());
    }


    private Condition mkSummaryCondition(Collection<Long> statisticIds,
                                         Select<Record1<Long>> appIdSelector,
                                         Condition additionalCondition) {
        return esv.STATISTIC_ID.in(statisticIds)
                    .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                    .and(esv.ENTITY_ID.in(appIdSelector))
                    .and(additionalCondition);
    }



    private Condition mkNoRollupCondition(Collection<Long> statisticIds,
                                          EntityReference ref,
                                          Condition additionalCondition) {
        return esv.STATISTIC_ID.in(statisticIds)
                .and(esv.ENTITY_KIND.eq(ref.kind().name()))
                .and(esv.ENTITY_ID.eq(ref.id()))
                .and(additionalCondition);
    }


    private Condition mkHistoryDurationCondition(Duration duration) {
        if (duration == Duration.ALL) {
            return DSL.trueCondition();
        }
        return esv.CREATED_AT.gt(currentTimestamp().minus(duration.numDays()));
    }

}