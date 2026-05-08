package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.person.Person;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowApprovers.class)
@JsonDeserialize(as = ImmutableProposedFlowApprovers.class)
public abstract class ProposedFlowApprovers {

    public abstract List<Person> sourceApprovers();

    public abstract List<Person> targetApprovers();

}