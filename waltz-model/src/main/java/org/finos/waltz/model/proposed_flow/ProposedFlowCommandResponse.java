package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.physical_flow.FlowAttributes;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.immutables.value.Value;
import org.jooq.Pro;

import java.util.Set;

import static java.util.Collections.emptySet;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowCommandResponse.class)
@JsonDeserialize(as = ImmutableProposedFlowCommandResponse.class)
public abstract class ProposedFlowCommandResponse implements Command {
    public abstract ProposedFlowCommand proposedFlowCommand();
    public abstract String outcome();
    public abstract String message();
    public abstract Long proposedFlowId();
}
