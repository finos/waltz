package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowActionCommand.class)
@JsonDeserialize(as = ImmutableProposedFlowActionCommand.class)
public abstract class ProposedFlowActionCommand implements Command {
    @Value.Default
    public String comment() {
        return "";
    }
}
