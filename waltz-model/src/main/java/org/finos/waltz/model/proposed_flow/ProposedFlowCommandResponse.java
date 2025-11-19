package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.command.CommandOutcome;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowCommandResponse.class)
@JsonDeserialize(as = ImmutableProposedFlowCommandResponse.class)
public abstract class ProposedFlowCommandResponse implements Command {
    public abstract ProposedFlowCommand proposedFlowCommand();

    public abstract CommandOutcome outcome();

    public abstract String message();

    public abstract Long proposedFlowId();

    public abstract Long workflowDefinitionId();
    @Nullable
    public abstract Long physicalFlowId();
}
