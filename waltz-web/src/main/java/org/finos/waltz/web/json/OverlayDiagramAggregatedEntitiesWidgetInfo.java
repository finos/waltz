package org.finos.waltz.web.json;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters.AggregatedEntitiesWidgetParameters;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramAggregatedEntitiesWidgetInfo.class)
@JsonDeserialize(as = ImmutableOverlayDiagramAggregatedEntitiesWidgetInfo.class)
public abstract class OverlayDiagramAggregatedEntitiesWidgetInfo extends OverlayDiagramWidgetInfo<AggregatedEntitiesWidgetParameters> {


}


