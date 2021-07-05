package com.khartec.waltz.model.external_identifier;

import com.khartec.waltz.model.Wrapped;
import com.khartec.waltz.model.Wrapper;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable @Wrapped
public abstract class _ExternalId extends Wrapper<String> {

    public static Optional<ExternalId> ofNullable(String str) {
        return Optional
                .ofNullable(str)
                .map(ExternalId::of);
    }


    public static String orElse(Optional<ExternalId> extId,
                                String dflt) {
        return extId
                .map(ExternalId::value)
                .orElse(dflt);
    }

}
