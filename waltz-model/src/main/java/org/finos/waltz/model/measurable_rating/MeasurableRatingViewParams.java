package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingViewParams.class)
@JsonDeserialize(as = ImmutableMeasurableRatingViewParams.class)
public abstract class MeasurableRatingViewParams {

    public abstract IdSelectionOptions idSelectionOptions();

    public abstract Optional<Long> parentMeasurableId();
}
