package com.khartec.waltz.model.dataflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDataFlowMeasures.class)
@JsonDeserialize(as = ImmutableDataFlowMeasures.class)
public abstract class DataFlowMeasures {

    public abstract double inbound();
    public abstract double outbound();
    public abstract double intra();

}
