package com.khartec.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingGridViewRatingItem.class)
@JsonDeserialize(as = ImmutableMeasurableRatingGridViewRatingItem.class)
public abstract class MeasurableRatingGridViewRatingItem {
    public abstract long measurableId();
    public abstract long applicationId();
    public abstract String rating();
}