package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.command.Command;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowCreateCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowCreateCommand.class)
public abstract class PhysicalFlowCreateCommand implements Command {

    public abstract PhysicalSpecification specification();
    public abstract EntityReference targetEntity();
    public abstract FlowAttributes flowAttributes();

}
