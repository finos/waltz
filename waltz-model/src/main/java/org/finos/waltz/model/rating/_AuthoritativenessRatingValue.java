package org.finos.waltz.model.rating;

import org.finos.waltz.model.Wrapped;
import org.finos.waltz.model.Wrapper;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable @Wrapped
public abstract class _AuthoritativenessRatingValue extends Wrapper<String> {

    public static AuthoritativenessRatingValue DISCOURAGED = AuthoritativenessRatingValue.of("DISCOURAGED");
    public static AuthoritativenessRatingValue NO_OPINION = AuthoritativenessRatingValue.of("NO_OPINION");

    public static Optional<AuthoritativenessRatingValue> ofNullable(String str) {
        return Optional
                .ofNullable(str)
                .map(AuthoritativenessRatingValue::of);
    }


    public static String orElse(Optional<AuthoritativenessRatingValue> ratingStr,
                                String dflt) {
        return ratingStr
                .map(AuthoritativenessRatingValue::value)
                .orElse(dflt);
    }

}

