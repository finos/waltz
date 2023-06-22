package org.finos.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableUpdateRatingCodeCommand.class)
public abstract class UpdateRatingCodeCommand implements Command {

    public abstract String newCode();

}
