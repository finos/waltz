package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.RatingCostWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramRatingCostWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramRatingCostWidgetInfo.class)
public abstract class OverlayDiagramRatingCostWidgetInfo extends OverlayDiagramWidgetInfo<RatingCostWidgetParameters> {


}


