package org.finos.waltz.model.aggregate_overlay_diagram.overlay.widget_parameters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAttestationWidgetParameters.class)
@JsonDeserialize(as = ImmutableAttestationWidgetParameters.class)
public abstract class AttestationWidgetParameters {

    @Nullable
    public abstract Long attestedEntityId();
    public abstract EntityKind attestedEntityKind();

}
