package org.finos.waltz.model.measurable_rating_planned_decommission;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.measurable_rating.MeasurableRating;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize (as = ImmutableMeasurableRatingPlannedDecommissionInfo.class)
@JsonDeserialize(as = ImmutableMeasurableRatingPlannedDecommissionInfo.class)
public abstract class MeasurableRatingPlannedDecommissionInfo {

    public abstract MeasurableRating measurableRating();
    public abstract MeasurableRatingPlannedDecommission decommission();
}
