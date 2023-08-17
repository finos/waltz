package org.finos.waltz.model.entity_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.aggregate_overlay_diagram.AggregateOverlayDiagram;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableAggregateOverlayDiagramInfo;
import org.immutables.value.Value;

import java.util.Set;

@JsonSerialize(as = ImmutableEntityOverlayDiagramInfo.class)
@Value.Immutable
public abstract class EntityOverlayDiagramInfo {

    public abstract EntityOverlayDiagram diagram();
    public abstract Set<OverlayBackingEntity> backingEntities();

}
