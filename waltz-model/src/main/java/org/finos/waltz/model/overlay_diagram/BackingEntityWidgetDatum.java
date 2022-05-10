package org.finos.waltz.model.overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableBackingEntityWidgetDatum.class)
public abstract class BackingEntityWidgetDatum {

    public abstract String cellExternalId();

    public abstract Set<EntityReference> backingEntityReferences();

}
