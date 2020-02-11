package com.khartec.waltz.model.measurable_rating_replacement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingReplacement.class)
@JsonDeserialize(as = ImmutableMeasurableRatingReplacement.class)
public abstract class MeasurableRatingReplacement implements
        LastUpdatedProvider,
        CreatedProvider {

    public abstract Long id();
    public abstract EntityReference entityReference();
    public abstract Long decommissionId();
    public abstract LocalDateTime plannedCommissionDate();

}
