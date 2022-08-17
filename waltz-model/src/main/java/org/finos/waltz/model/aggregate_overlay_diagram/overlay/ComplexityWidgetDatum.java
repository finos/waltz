package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityWidgetDatum.class)
public abstract class ComplexityWidgetDatum implements CellExternalIdProvider {


    @Value.Derived
    public BigDecimal totalComplexity() {
        return complexities()
                .stream()
                .map(ComplexityEntry::complexityScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Value.Derived
    public BigDecimal averageComplexity() {

        int complexityCount = complexities().size();

        return complexityCount == 0
                ? BigDecimal.ZERO
                : totalComplexity().divide(BigDecimal.valueOf(complexityCount), RoundingMode.HALF_UP);
    }

    public abstract Set<ComplexityEntry> complexities();

}
