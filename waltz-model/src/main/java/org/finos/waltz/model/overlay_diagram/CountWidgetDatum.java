package org.finos.waltz.model.overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableCountWidgetDatum.class)
public abstract class CountWidgetDatum {

    public abstract String cellExternalId();
    public abstract int currentStateCount();
    public abstract int targetStateCount();

}
