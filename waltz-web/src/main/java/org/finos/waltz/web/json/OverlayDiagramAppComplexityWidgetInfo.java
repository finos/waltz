package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppComplexityWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAppComplexityWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAppComplexityWidgetInfo.class)
public abstract class OverlayDiagramAppComplexityWidgetInfo extends OverlayDiagramWidgetInfo<AppComplexityWidgetParameters> {


}


