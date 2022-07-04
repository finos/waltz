package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableTargetCostWidgetDatum.class)
public abstract class TargetCostWidgetDatum implements CellExternalIdProvider {

    public abstract BigDecimal currentStateCost();

    public abstract BigDecimal targetStateCost();

}
