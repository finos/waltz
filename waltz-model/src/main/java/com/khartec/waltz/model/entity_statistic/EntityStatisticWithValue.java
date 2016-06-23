package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticWithValue.class)
@JsonDeserialize(as = ImmutableEntityStatisticWithValue.class)
public abstract class EntityStatisticWithValue {

    public abstract EntityStatistic statistic();
    public abstract EntityStatisticValue value();
}
