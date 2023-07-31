package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.entity_overlay_diagram.OverlayDiagramKind;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregateOverlayDiagram.class)
@JsonDeserialize(as = ImmutableAggregateOverlayDiagram.class)
public abstract class AggregateOverlayDiagram implements IdProvider, NameProvider, DescriptionProvider, LastUpdatedProvider, ProvenanceProvider {

    public abstract String layoutData();

    public abstract EntityKind aggregatedEntityKind();

    public abstract OverlayDiagramKind diagramKind();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.AGGREGATE_OVERLAY_DIAGRAM;
    }
}
