package com.khartec.waltz.model.allocation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Operation;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurablePercentageChange.class)
@JsonDeserialize(as = ImmutableMeasurablePercentageChange.class)
public abstract class MeasurablePercentageChange {

    public abstract Operation operation();

    public abstract MeasurablePercentage measurablePercentage();
}
