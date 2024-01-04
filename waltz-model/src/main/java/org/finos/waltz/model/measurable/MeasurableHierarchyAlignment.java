package org.finos.waltz.model.measurable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableHierarchyAlignment.class)
public abstract class MeasurableHierarchyAlignment {
    public abstract EntityReference parentReference();
    public abstract Integer level();
}
