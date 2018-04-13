package com.khartec.waltz.model.shared_preference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableSharedPreference.class)
@JsonDeserialize(as = ImmutableSharedPreference.class)
public abstract class SharedPreference implements LastUpdatedProvider {
    public abstract String key();
    public abstract String category();
    public abstract String value();
}
