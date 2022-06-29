package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregatedEntitiesWidgetDatum.class)
public abstract class AggregatedEntitiesWidgetDatum implements CellExternalIdProvider {

    public abstract Set<EntityReference> aggregatedEntityReferences();

}
