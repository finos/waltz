package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableAppCountWidgetParameters.class)
@JsonDeserialize(as = ImmutableAppCountWidgetParameters.class)
public abstract class AppCountWidgetParameters {

    public abstract LocalDate targetDate();

}
