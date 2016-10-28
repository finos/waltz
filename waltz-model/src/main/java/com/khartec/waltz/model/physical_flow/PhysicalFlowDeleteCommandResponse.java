package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.AbstractCommandResponse;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowDeleteCommandResponse.class)
@JsonDeserialize(as = ImmutablePhysicalFlowDeleteCommandResponse.class)
public abstract class PhysicalFlowDeleteCommandResponse extends AbstractCommandResponse<PhysicalFlowDeleteCommand> {

    public abstract boolean isSpecificationUnused();
}
