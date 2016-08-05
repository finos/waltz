package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.entity_statistic.EntityStatisticDefinition;
import com.khartec.waltz.model.entity_statistic.EntityStatisticSummary;
import com.khartec.waltz.model.entity_statistic.ImmutableEntityStatisticSummary;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;

@Repository
public class EntityStatisticSummaryDao {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition esd = ENTITY_STATISTIC_DEFINITION.as("esd");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    private static Field<Integer> COUNT = DSL.field("count", Integer.class);


    private static final Function<? super Map.Entry<Record, Result<Record>>, EntityStatisticSummary> TO_SUMMARY_MAPPER = recordResultEntry -> {
        EntityStatisticDefinition def = EntityStatisticDefinitionDao.TO_DEFINITION_MAPPER.map(recordResultEntry.getKey());

        List<StringTally> counts = recordResultEntry.getValue()
                .into(esv.field(esv.OUTCOME), COUNT)
                .map(TO_STRING_TALLY);

        return ImmutableEntityStatisticSummary.builder()
                .definition(def)
                .counts(counts)
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public EntityStatisticSummaryDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<StringTally> generateWithSumByValue(Long statisticId, Select<Record1<Long>> appIdSelector) {
        Field<BigDecimal> total = DSL.sum(DSL.cast(esv.VALUE, Long.class)).as("total");
        Function<BigDecimal, Double> toTally = v -> v.doubleValue();

        return generateSummary(statisticId, appIdSelector, total, toTally);
    }


    public List<TallyPack<String>> generateWithSumByValue(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        Field<BigDecimal> total = DSL.sum(DSL.cast(esv.VALUE, Long.class)).as("total");
        Function<BigDecimal, Double> toTally = v -> v.doubleValue();

        return generateSummaries(statisticIds, appIdSelector, total, toTally);
    }


    public List<TallyPack<String>> generateWithCountByEntity(Collection<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        Field<Integer> total = DSL.count();
        Function<Integer, Double> toTally = v -> v.doubleValue();

        return generateSummaries(statisticIds, appIdSelector, total, toTally);
    }

    public List<StringTally> generateWithCountByEntity(Long statisticId, Select<Record1<Long>> appIdSelector) {
        Field<Integer> total = DSL.count();
        Function<Integer, Double> toTally = v -> v.doubleValue();

        return generateSummary(statisticId, appIdSelector, total, toTally);
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