package com.khartec.waltz.model.command;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableFieldChange.class)
@JsonDeserialize(as = ImmutableFieldChange.class)
public abstract class FieldChange<T> {

    @Nullable
    public abstract T newVal();

    @Nullable
    public abstract T oldVal();

    @Nullable
    public abstract String description();

}
