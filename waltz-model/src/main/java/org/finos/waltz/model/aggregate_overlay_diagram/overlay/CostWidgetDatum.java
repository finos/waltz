package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableCostWidgetDatum.class)
public abstract class CostWidgetDatum {

    public abstract String cellExternalId();
    public abstract BigDecimal totalCost();

    public abstract int appCount();

}
