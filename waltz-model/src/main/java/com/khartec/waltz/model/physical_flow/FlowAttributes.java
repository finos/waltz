package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowAttributes.class)
@JsonDeserialize(as = ImmutableFlowAttributes.class)
public abstract class FlowAttributes implements DescriptionProvider{

    public abstract TransportKind transport();
    public abstract FrequencyKind frequency();
    public abstract int basisOffset();

}
