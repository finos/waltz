package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowDeleteCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowDeleteCommand.class)
public abstract class PhysicalFlowDeleteCommand implements Command {

    public abstract long flowId();
}
