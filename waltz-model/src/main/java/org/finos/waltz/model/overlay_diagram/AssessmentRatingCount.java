package org.finos.waltz.model.overlay_diagram;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentRatingCount.class)
public abstract class AssessmentRatingCount {

    public abstract int count();
    public abstract RatingSchemeItem rating();
}

