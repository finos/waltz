package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.SummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticSummary.class)
@JsonDeserialize(as = ImmutableEntityStatisticSummary.class)
public abstract class EntityStatisticSummary implements SummaryStatistics {

    public abstract EntityStatisticDefinition definition();
    public abstract List<StringTally> counts();
}
