package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowSpecDefinitionChangeCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowSpecDefinitionChangeCommand.class)
public abstract class PhysicalFlowSpecDefinitionChangeCommand {

    public abstract long newSpecDefinitionId();
}
