package com.khartec.waltz.model.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCommandResponse.class)
@JsonDeserialize(as = ImmutableCommandResponse.class)
public abstract class CommandResponse<T extends Command> extends AbstractCommandResponse<T> {

}
