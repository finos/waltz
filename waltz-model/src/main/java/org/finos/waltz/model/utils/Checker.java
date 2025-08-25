package org.finos.waltz.model.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.proposed_flow.ImmutableProposedFlowCommandResponse;
import org.finos.waltz.model.proposed_flow.ProposedFlowCommand;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableChecker.class)
@JsonDeserialize(as = ImmutableChecker.class)
public abstract class Checker {
    public abstract Set<Operation> sourceApprover();
    public abstract Set<Operation> targetApprover();

}
