package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.TargetAppCostWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramTargetAppCostWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramTargetAppCostWidgetInfo.class)
public abstract class OverlayDiagramTargetAppCostWidgetInfo extends OverlayDiagramWidgetInfo<TargetAppCostWidgetParameters> {


}


