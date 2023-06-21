package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableMeasurableRatingStatParams.class)
public abstract class MeasurableRatingStatParams {

    public abstract IdSelectionOptions options();


    @Value.Default
    public boolean showPrimaryOnly() {
        return false;
    }

}
