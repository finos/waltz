package com.khartec.waltz.model.command;


import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Optional;

public abstract class AbstractCommandResponse<T> {
    public abstract EntityReference entityReference();

    public abstract T originalCommand();

    @Value.Default
    public CommandOutcome outcome() {
        return CommandOutcome.SUCCESS;
    }

    public abstract Optional<String> message();
}
