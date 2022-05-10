package org.finos.waltz.model.overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.IdSelectionOptions;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAppCostWidgetParameters.class)
@JsonDeserialize(as = ImmutableAppCostWidgetParameters.class)
public abstract class AppCostWidgetParameters {

    public abstract Long allocationSchemeId();

    public abstract Set<Long> costKindIds();

    public abstract IdSelectionOptions selectionOptions();

}
