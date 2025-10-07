package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlowCreateCommandResponse;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableLogicalPhysicalFlowCreationResponse.class)
@JsonDeserialize(as = ImmutableLogicalPhysicalFlowCreationResponse.class)
public abstract class LogicalPhysicalFlowCreationResponse implements Command {

    public abstract LogicalFlow logicalFlow();

    public abstract PhysicalFlowCreateCommandResponse physicalFlowCreateCommandResponse();
}
