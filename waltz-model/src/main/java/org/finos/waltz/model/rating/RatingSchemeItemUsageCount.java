package org.finos.waltz.model.rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRatingSchemeItemUsageCount.class)
public abstract class RatingSchemeItemUsageCount {

    public abstract long schemeId();
    public abstract long ratingId();
    public abstract EntityKind usageKind();
    public abstract int count();

}

