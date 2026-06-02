package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.person.Person;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableApproverWithType.class)
@JsonDeserialize(as = ImmutableApproverWithType.class)
public abstract class ApproverWithType {
    public abstract Person person();
    public abstract String approverType();
    public abstract Long involvementKindId();
    public abstract String involvementKindName();
}