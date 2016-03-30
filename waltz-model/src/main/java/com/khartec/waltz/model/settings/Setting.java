package com.khartec.waltz.model.settings;

import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class Setting implements NameProvider {

    public abstract Optional<String> value();
    public abstract SettingKind kind();

}
