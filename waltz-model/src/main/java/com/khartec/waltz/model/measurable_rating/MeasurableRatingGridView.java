package com.khartec.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.rating.RagName;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingGridView.class)
@JsonDeserialize(as = ImmutableMeasurableRatingGridView.class)
public abstract class MeasurableRatingGridView {


    public abstract MeasurableCategory category();

    public abstract Set<Application> applications();

    public abstract Set<EntityReference> measurableReferences();

    public abstract Set<RagName> ratingSchemeItems();

    public abstract Set<MeasurableRatingGridViewRatingItem> ratings();

}
