package com.khartec.waltz.model.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableUserPreference.class)
@JsonDeserialize(as = ImmutableUserPreference.class)
public abstract class UserPreference {
    public abstract String key();
    public abstract String value();
}
