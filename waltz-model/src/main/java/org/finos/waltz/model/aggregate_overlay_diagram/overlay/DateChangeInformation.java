package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDateChangeInformation.class)
public abstract class DateChangeInformation {

    public abstract QuarterDetail quarter();
    public abstract int count();

}