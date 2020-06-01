package com.khartec.waltz.model.measurable_rating_planned_decommission;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize (as = ImmutableMeasurableRatingPlannedDecommission.class)
@JsonDeserialize(as = ImmutableMeasurableRatingPlannedDecommission.class)
public abstract class MeasurableRatingPlannedDecommission implements
        LastUpdatedProvider,
        CreatedProvider {

    public abstract Long id();
    public abstract EntityReference entityReference();
    public abstract Long measurableId();
    public abstract LocalDate plannedDecommissionDate();
}
