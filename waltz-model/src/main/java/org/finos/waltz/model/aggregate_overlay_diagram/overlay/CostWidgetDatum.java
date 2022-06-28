package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableCostWidgetDatum.class)
public abstract class CostWidgetDatum implements CellExternalIdProvider {

    @Value.Derived
    public BigDecimal totalCost() {
        return measurableCosts()
                .stream()
                .map(MeasurableCostEntry::allocatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Value.Derived
    public long appCount() {
        return measurableCosts()
                .stream()
                .map(MeasurableCostEntry::appId)
                .distinct()
                .count();
    }

    public abstract Set<MeasurableCostEntry> measurableCosts();

}
