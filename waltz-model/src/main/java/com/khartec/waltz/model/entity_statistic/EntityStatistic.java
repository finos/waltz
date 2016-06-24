package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatistic.class)
@JsonDeserialize(as = ImmutableEntityStatistic.class)
public abstract class EntityStatistic {

    public abstract EntityStatisticDefinition statistic();
    public abstract EntityStatisticValue value();
}
