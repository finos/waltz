package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as= ImmutableAssessmentRatingsWidgetData.class)
public abstract class AssessmentRatingsWidgetData implements CellDataProvider<AssessmentRatingsWidgetDatum> {


}
