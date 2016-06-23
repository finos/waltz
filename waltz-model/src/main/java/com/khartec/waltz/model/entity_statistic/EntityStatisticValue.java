package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticValue.class)
@JsonDeserialize(as = ImmutableEntityStatisticValue.class)
public abstract class EntityStatisticValue implements IdProvider, ProvenanceProvider {

    public abstract long statisticId();
    public abstract EntityReference entity();
    public abstract String value();
    public abstract String outcome();
    public abstract StatisticValueState state();
    @Nullable
    public abstract String reason();
    public abstract LocalDateTime createdAt();
    public abstract boolean current();
}
