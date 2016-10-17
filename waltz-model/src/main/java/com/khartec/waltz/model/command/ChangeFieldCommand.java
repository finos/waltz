package com.khartec.waltz.model.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.Optional;


@Value.Immutable
@JsonSerialize(as = ImmutableChangeFieldCommand.class)
@JsonDeserialize(as = ImmutableChangeFieldCommand.class)
public abstract class ChangeFieldCommand {

    public abstract EntityReference entityReference();
    public abstract String fieldName();
    public abstract String newVal();

    @Nullable
    public abstract String oldVal();

    public abstract Optional<LastUpdate> lastUpdate();
}
