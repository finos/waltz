package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AssessmentWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAssessmentWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAssessmentWidgetInfo.class)
public abstract class OverlayDiagramAssessmentWidgetInfo extends OverlayDiagramWidgetInfo<AssessmentWidgetParameters> {


}


