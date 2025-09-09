package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowCommandResponse.class)
@JsonDeserialize(as = ImmutableProposedFlowCommandResponse.class)
public abstract class ProposedFlowCommandResponse implements Command {
    public abstract ProposedFlowCommand proposedFlowCommand();
    public abstract String outcome();
    public abstract String message();
    public abstract Long proposedFlowId();
    public abstract Long workflowDefinitionId();
    public abstract String proposalType();
}
