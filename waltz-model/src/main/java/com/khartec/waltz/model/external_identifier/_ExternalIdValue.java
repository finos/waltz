package com.khartec.waltz.model.external_identifier;

import com.khartec.waltz.model.Wrapped;
import com.khartec.waltz.model.Wrapper;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable @Wrapped
public abstract class _ExternalIdValue extends Wrapper<String> {

    public static Optional<ExternalIdValue> ofNullable(String str) {
        return Optional
                .ofNullable(str)
                .map(ExternalIdValue::of);
    }


    public static String orElse(Optional<ExternalIdValue> extId,
                                String dflt) {
        return extId
                .map(ExternalIdValue::value)
                .orElse(dflt);
    }

}
