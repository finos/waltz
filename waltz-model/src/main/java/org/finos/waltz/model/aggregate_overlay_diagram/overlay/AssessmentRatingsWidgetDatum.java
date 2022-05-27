package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAssessmentRatingsWidgetDatum.class)
public abstract class AssessmentRatingsWidgetDatum {

    public abstract String cellExternalId();
    public abstract Set<AssessmentRatingCount> counts();

}
