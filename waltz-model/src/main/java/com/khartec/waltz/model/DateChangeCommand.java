package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableDateChangeCommand.class)
@JsonDeserialize(as = ImmutableDateChangeCommand.class)
public abstract class DateChangeCommand {

    public abstract Optional<LocalDate> newDateVal();
}
