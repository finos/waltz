package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregatedEntitiesWidgetParameters.class)
@JsonDeserialize(as = ImmutableAggregatedEntitiesWidgetParameters.class)
public abstract class AggregatedEntitiesWidgetParameters {

}
