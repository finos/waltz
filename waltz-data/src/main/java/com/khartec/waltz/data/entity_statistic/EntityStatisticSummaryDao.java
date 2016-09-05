package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.ImmutableTallyPack;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.model.tally.TallyPack;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticSummaryDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    private static final Field<BigDecimal> avgTotal = DSL.avg(DSL.cast(esv.VALUE, Long.class)).as("total");
    private static final Field<BigDecimal> sumTotal = DSL.sum(DSL.cast(esv.VALUE, Long.class)).as("total");
    private static final Field<Integer> countTotal = DSL.count();
    private static final Function<BigDecimal, Double> toBigDecimalTally = v -> v.doubleValue();
    private static final Function<Integer, Double> toIntegerTally = v -> v.doubleValue();

    private final DSLContext dsl;


    @Autowired
    public EntityStatisticSummaryDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<StringTally> generateWithAvgByValue(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, avgTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateWithAvgByValue(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, avgTotal, toBigDecimalTally);
    }


    public List<StringTally> generateWithSumByValue(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, sumTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateWithSumByValue(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, sumTotal, toBigDecimalTally);
    }


    public List<TallyPack<String>> generateWithCountByEntity(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        return generateSummaries(statisticIds, appIdSelector, countTotal, toIntegerTally);
    }


    public List<TallyPack<String>> generateWithNoRollup(Collection<Long> statisticIds, EntityReference entityReference) {
        Condition condition = esv.STATISTIC_ID.in(statisticIds)
                .and(esv.ENTITY_KIND.eq(entityReference.kind().name()))
                .and(esv.ENTITY_ID.eq(entityReference.id()))
                .and(esv.CURRENT.eq(true));

        Select<Record3<Long, String, String>> values = dsl
                .select(esv.STATISTIC_ID, esv.OUTCOME, esv.VALUE)
                .from(esv)
                .where(dsl.renderInlined(condition));

        return values.fetch()
                .intoGroups(esv.STATISTIC_ID, r -> ImmutableStringTally.builder()
                        .count(Double.parseDouble(r.getValue(esv.VALUE)))
                        .id(r.getValue(esv.OUTCOME))
                        .build())
                .entrySet()
                .stream()
                .map(entry -> ImmutableTallyPack.<String>builder()
                        .entityReference(ImmutableEntityReference.builder()
                                .kind(EntityKind.ENTITY_STATISTIC)
                                .id(entry.getKey())
                                .build())
                        .tallies(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }


    public List<StringTally> generateWithCountByEntity(Long statisticId, Select<Record1<Long>> appIdSelector) {
        return generateSummary(statisticId, appIdSelector, countTotal, toIntegerTally);
    }


    private <T> List<StringTally> generateSummary(
            Long statisticId,
            Select<Record1<Long>> appIdSelector,
            Field<T> aggregateField,
            Function<T, Double> toTally) {

        Condition condition = esv.STATISTIC_ID.eq(statisticId)
                .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(esv.ENTITY_ID.in(appIdSelector))
                .and(esv.CURRENT.eq(true));

        return dsl.select(esv.OUTCOME, aggregateField)
                .from(esv)
                .where(condition)
                .groupBy(esv.OUTCOME)
                .fetch()
                .stream()
                .map(r -> ImmutableStringTally.builder()
                        .count(toTally.apply(r.getValue(aggregateField)))
                        .id(r.getValue(esv.OUTCOME))
                        .build())
                .collect(Collectors.toList());
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

        Condition condition = esv.STATISTIC_ID.in(statisticIds)
                .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(esv.ENTITY_ID.in(appIdSelector))
                .and(esv.CURRENT.eq(true));


        SelectHavingStep<Record3<Long, String, T>> aggregates = dsl
                .select(esv.STATISTIC_ID, esv.OUTCOME, aggregateField)
                .from(esv)
                .where(dsl.renderInlined(condition))
                .groupBy(esv.STATISTIC_ID, esv.OUTCOME);

        return aggregates.fetch()
                .intoGroups(esv.STATISTIC_ID, r -> ImmutableStringTally.builder()
                        .count(toTally.apply(r.getValue(aggregateField)))
                        .id(r.getValue(esv.OUTCOME))
                        .build())
                .entrySet()
                .stream()
                .map(entry -> ImmutableTallyPack.<String>builder()
                        .entityReference(ImmutableEntityReference.builder()
                                .kind(EntityKind.ENTITY_STATISTIC)
                                .id(entry.getKey())
                                .build())
                        .tallies(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

}