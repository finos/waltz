package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowApprover.class)
@JsonDeserialize(as = ImmutableProposedFlowApprover.class)
public abstract class ProposedFlowApprover {

    // Person Details
    public abstract Person person();

    // Involvement Kind Details
    public abstract Long involvementKindId();
    public abstract String involvementKindName();

}
