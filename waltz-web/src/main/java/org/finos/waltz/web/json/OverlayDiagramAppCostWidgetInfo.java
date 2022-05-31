package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCostWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAppCostWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAppCostWidgetInfo.class)
public abstract class OverlayDiagramAppCostWidgetInfo extends OverlayDiagramWidgetInfo<AppCostWidgetParameters> {


}


