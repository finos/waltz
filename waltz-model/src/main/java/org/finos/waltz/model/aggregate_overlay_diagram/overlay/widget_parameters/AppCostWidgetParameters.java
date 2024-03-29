package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAppCostWidgetParameters.class)
@JsonDeserialize(as = ImmutableAppCostWidgetParameters.class)
public abstract class AppCostWidgetParameters {

    public abstract Long allocationSchemeId();
    public abstract Set<Long> costKindIds();

}
