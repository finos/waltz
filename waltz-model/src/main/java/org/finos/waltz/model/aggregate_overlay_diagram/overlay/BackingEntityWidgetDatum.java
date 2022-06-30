package org.finos.waltz.model.aggregate_overlay_diagram.overlay;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableBackingEntityWidgetDatum.class)
public abstract class BackingEntityWidgetDatum implements CellExternalIdProvider {

    public abstract Set<EntityReference> backingEntityReferences();

}
