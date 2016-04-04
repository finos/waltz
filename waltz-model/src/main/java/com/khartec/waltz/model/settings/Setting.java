package com.khartec.waltz.model.settings;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableSetting.class)
@JsonDeserialize(as = ImmutableSetting.class)
public abstract class Setting implements NameProvider {

    public abstract Optional<String> value();
    public abstract boolean restricted();

}
