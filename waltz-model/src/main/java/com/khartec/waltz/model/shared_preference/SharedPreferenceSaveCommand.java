package com.khartec.waltz.model.shared_preference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableSharedPreferenceSaveCommand.class)
@JsonDeserialize(as = ImmutableSharedPreferenceSaveCommand.class)
public abstract class SharedPreferenceSaveCommand implements Command {
    public abstract String key();
    public abstract String category();
    public abstract String value();
}
