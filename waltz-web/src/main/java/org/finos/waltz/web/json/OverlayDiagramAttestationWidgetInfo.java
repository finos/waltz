package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AttestationWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAttestationWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAttestationWidgetInfo.class)
public abstract class OverlayDiagramAttestationWidgetInfo extends OverlayDiagramWidgetInfo<AttestationWidgetParameters> {


}


