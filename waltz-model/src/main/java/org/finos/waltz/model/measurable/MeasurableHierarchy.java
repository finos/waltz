package org.finos.waltz.model.measurable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Comparator;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableHierarchy.class)
public abstract class MeasurableHierarchy {

    public abstract Long measurableId();
    public abstract Set<MeasurableHierarchyAlignment> parents();

    @Value.Derived
    public Integer maxDepth() {
        return parents()
                .stream()
                .map(MeasurableHierarchyAlignment::level)
                .max(Comparator.comparing(d -> d))
                .orElse(1);
    }

}
