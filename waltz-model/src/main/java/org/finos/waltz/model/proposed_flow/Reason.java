package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableReason.class)
@JsonDeserialize(as = ImmutableReason.class)
public abstract class Reason implements Command {
    public abstract String description();
    public abstract int ratingId();

}
