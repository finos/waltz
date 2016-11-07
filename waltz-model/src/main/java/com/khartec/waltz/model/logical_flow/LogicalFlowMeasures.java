package com.khartec.waltz.model.logical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLogicalFlowMeasures.class)
@JsonDeserialize(as = ImmutableLogicalFlowMeasures.class)
public abstract class LogicalFlowMeasures {

    public abstract double inbound();
    public abstract double outbound();
    public abstract double intra();

}
