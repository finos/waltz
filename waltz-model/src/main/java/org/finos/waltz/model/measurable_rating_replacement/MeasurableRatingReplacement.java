package org.finos.waltz.model.measurable_rating_replacement;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.CreatedProvider;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.LastUpdatedProvider;
import org.immutables.value.Value;

import java.util.Date;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingReplacement.class)
@JsonDeserialize(as = ImmutableMeasurableRatingReplacement.class)
public abstract class MeasurableRatingReplacement implements
        LastUpdatedProvider,
        CreatedProvider {

    public abstract Long id();
    public abstract EntityReference entityReference();
    public abstract Long decommissionId();
    public abstract Date plannedCommissionDate();

}
