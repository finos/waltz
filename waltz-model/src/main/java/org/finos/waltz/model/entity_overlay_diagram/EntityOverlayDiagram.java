package org.finos.waltz.model.entity_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.LastUpdatedProvider;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityOverlayDiagram.class)
@JsonDeserialize(as = ImmutableEntityOverlayDiagram.class)
public abstract class EntityOverlayDiagram implements IdProvider, NameProvider, DescriptionProvider, LastUpdatedProvider, ProvenanceProvider {

    public abstract String layoutData();

    public abstract OverlayDiagramKind diagramKind();

    public abstract EntityKind aggregatedEntityKind();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.ENTITY_OVERLAY_DIAGRAM;
    }
}
