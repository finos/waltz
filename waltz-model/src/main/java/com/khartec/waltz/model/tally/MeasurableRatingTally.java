package com.khartec.waltz.model.tally;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.rating.RagRating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingTally.class)
@JsonDeserialize(as = ImmutableMeasurableRatingTally.class)
public abstract class MeasurableRatingTally {

    public abstract long id();
    public abstract RagRating rating();
    public abstract long count();

}
