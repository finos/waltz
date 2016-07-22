package com.khartec.waltz.data.entity_statistic;

import com.khartec.waltz.common.Checks;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.TO_STRING_TALLY;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;
import static org.jooq.impl.DSL.count;

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


    public List<TallyPack<String>> findStatTallies(List<Long> statisticIds, Select<Record1<Long>> appIdSelector) {
        Checks.checkNotNull(statisticIds, "statisticIds cannot be null");
        Checks.checkNotNull(appIdSelector, "appIdSelector cannot be null");


        Condition condition = esv.STATISTIC_ID.in(statisticIds)
                .and(esv.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(esv.ENTITY_ID.in(appIdSelector))
                .and(esv.CURRENT.eq(true));


        SelectHavingStep<Record3<Long, String, Integer>> aggregates = dsl
                .select(esv.STATISTIC_ID, esv.OUTCOME, count().as("count"))
                .from(esv)
                .where(dsl.renderInlined(condition))
                .groupBy(esv.STATISTIC_ID, esv.OUTCOME);

        return aggregates.fetch()
                .intoGroups(esv.STATISTIC_ID, r -> ImmutableStringTally.builder()
                        .count(r.getValue(COUNT))
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