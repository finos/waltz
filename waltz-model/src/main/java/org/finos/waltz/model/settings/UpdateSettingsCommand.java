package org.finos.waltz.model.settings;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableUpdateSettingsCommand.class)
@JsonDeserialize(as = ImmutableUpdateSettingsCommand.class)
public abstract class UpdateSettingsCommand implements Command {

    public abstract String name();
    public abstract String value();
    @Nullable public abstract String description();

}
