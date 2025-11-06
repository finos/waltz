package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowIdResponse.class)
@JsonDeserialize(as = ImmutableFlowIdResponse.class)
public abstract class FlowIdResponse {
    public abstract Long id();
    public abstract EntityKind type();

}
