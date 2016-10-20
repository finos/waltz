package com.khartec.waltz.model.physical_flow_lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowLineageRemoveCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowLineageRemoveCommand.class)
public abstract class PhysicalFlowLineageRemoveCommand implements Command {

    public abstract long describedFlowId();
    public abstract long contributingFlowId();
}
