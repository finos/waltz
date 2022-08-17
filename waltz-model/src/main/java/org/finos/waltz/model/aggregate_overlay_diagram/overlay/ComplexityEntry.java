package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityEntry.class)
public abstract class ComplexityEntry {

    public abstract long appId();

    @Nullable
    public abstract BigDecimal complexityScore();

}