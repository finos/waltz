package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.measurable_rating_planned_decommission.MeasurableRatingPlannedDecommission;
import org.finos.waltz.model.measurable_rating_replacement.MeasurableRatingReplacement;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingView.class)
public abstract class MeasurableRatingView {

    public abstract MeasurableRating measurableRating();

    @Nullable
    public abstract EntityReference measurable();

    @Nullable
    public abstract RatingSchemeItem rating();

    @Nullable
    public abstract MeasurableRatingPlannedDecommission decommission();

    public abstract Set<MeasurableRatingReplacement> replacements();

}
