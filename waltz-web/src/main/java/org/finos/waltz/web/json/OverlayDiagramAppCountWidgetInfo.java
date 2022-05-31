package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AppCountWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAppCountWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAppCountWidgetInfo.class)
public abstract class OverlayDiagramAppCountWidgetInfo extends OverlayDiagramWidgetInfo<AppCountWidgetParameters> {

}


