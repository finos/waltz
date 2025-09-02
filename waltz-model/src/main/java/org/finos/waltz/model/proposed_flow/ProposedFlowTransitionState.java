package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowTransitionState.class)
@JsonDeserialize(as = ImmutableProposedFlowTransitionState.class)
public abstract class ProposedFlowTransitionState implements LastUpdatedProvider
{
    public abstract String reason();
}
