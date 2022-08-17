package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAppComplexityWidgetParameters.class)
@JsonDeserialize(as = ImmutableAppComplexityWidgetParameters.class)
public abstract class AppComplexityWidgetParameters {

    public abstract Long complexityKindId();

}
