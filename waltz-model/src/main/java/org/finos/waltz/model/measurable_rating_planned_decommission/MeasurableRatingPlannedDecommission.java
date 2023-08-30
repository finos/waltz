package org.finos.waltz.model.measurable_rating_planned_decommission;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.CreatedProvider;
import org.finos.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@JsonSerialize (as = ImmutableMeasurableRatingPlannedDecommission.class)
@JsonDeserialize(as = ImmutableMeasurableRatingPlannedDecommission.class)
public abstract class MeasurableRatingPlannedDecommission implements
        LastUpdatedProvider,
        CreatedProvider {

    public abstract Long id();
    public abstract Long measurableRatingId();
    public abstract Date plannedDecommissionDate();
}
