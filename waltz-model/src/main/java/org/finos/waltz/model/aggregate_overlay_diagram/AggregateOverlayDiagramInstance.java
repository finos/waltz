package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregateOverlayDiagramInstance.class)
@JsonDeserialize(as = ImmutableAggregateOverlayDiagramInstance.class)
public abstract class AggregateOverlayDiagramInstance implements IdProvider, NameProvider, DescriptionProvider, LastUpdatedProvider, ProvenanceProvider {

    public abstract Long diagramId();

    public abstract EntityReference parentEntityReference();

    public abstract String svg();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.AGGREGATE_OVERLAY_DIAGRAM_INSTANCE;
    }
}
