package com.khartec.waltz.model.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableCommandResponse.class)
@JsonDeserialize(as = ImmutableCommandResponse.class)
public abstract class CommandResponse<T extends Command> {

    public abstract EntityReference entityReference();

    public abstract T originalCommand();

    @Value.Default
    public CommandOutcome outcome() {
        return CommandOutcome.SUCCESS;
    }

    public abstract Optional<String> message();


}
