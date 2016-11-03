package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.AbstractCommandResponse;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowCreateCommandResponse.class)
@JsonDeserialize(as = ImmutablePhysicalFlowCreateCommandResponse.class)
public abstract class PhysicalFlowCreateCommandResponse extends AbstractCommandResponse<PhysicalFlowCreateCommand> {

}
