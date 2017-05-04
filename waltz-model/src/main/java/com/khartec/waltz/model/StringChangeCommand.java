package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableStringChangeCommand.class)
@JsonDeserialize(as = ImmutableStringChangeCommand.class)
public abstract class StringChangeCommand {

    public abstract Optional<String> newStringVal();
}
