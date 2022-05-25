package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@JsonSerialize(as = ImmutableAggregateOverlayDiagramInfo.class)
@Value.Immutable
public abstract class AggregateOverlayDiagramInfo {

    public abstract AggregateOverlayDiagram diagram();
    public abstract Set<BackingEntity> backingEntities();

}
