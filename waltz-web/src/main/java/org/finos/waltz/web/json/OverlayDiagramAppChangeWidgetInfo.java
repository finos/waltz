package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppChangeWidgetParameters;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.TargetAppCostWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAppChangeWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAppChangeWidgetInfo.class)
public abstract class OverlayDiagramAppChangeWidgetInfo extends OverlayDiagramWidgetInfo<AppChangeWidgetParameters> {


}


