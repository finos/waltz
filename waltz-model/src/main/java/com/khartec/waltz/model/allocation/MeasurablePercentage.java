package com.khartec.waltz.model.allocation;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurablePercentage.class)
@JsonDeserialize(as = ImmutableMeasurablePercentage.class)
public abstract class MeasurablePercentage {

    public abstract int percentage();
    public abstract long measurableId();


    public static MeasurablePercentage mkMeasurablePercentage(long measurableId, int percentage) {
        return ImmutableMeasurablePercentage
                .builder()
                .percentage(percentage)
                .measurableId(measurableId)
                .build();
    }

}
