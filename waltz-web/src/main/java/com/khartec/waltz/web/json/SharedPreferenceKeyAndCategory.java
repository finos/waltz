package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableSharedPreferenceKeyAndCategory.class)
@JsonDeserialize(as = ImmutableSharedPreferenceKeyAndCategory.class)
public abstract class SharedPreferenceKeyAndCategory {

    public abstract String key();
    public abstract String category();
}
