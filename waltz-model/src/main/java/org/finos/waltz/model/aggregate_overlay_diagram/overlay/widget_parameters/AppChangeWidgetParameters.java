package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableAppChangeWidgetParameters.class)
@JsonDeserialize(as = ImmutableAppChangeWidgetParameters.class)
public abstract class AppChangeWidgetParameters {

    public abstract LocalDate targetDate();

}
