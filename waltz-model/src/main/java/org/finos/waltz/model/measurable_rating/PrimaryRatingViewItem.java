package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePrimaryRatingViewItem.class)
public abstract class PrimaryRatingViewItem {

    public abstract EntityReference measurableCategory();
    public abstract EntityReference measurable();
    public abstract String ratingName();
    public abstract String ratingDescription();
    public abstract String ratingColor();

}
