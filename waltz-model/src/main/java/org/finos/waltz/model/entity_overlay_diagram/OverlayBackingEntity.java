package org.finos.waltz.model.entity_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.aggregate_overlay_diagram.ImmutableBackingEntity;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableBackingEntity.class)
@Value.Immutable
public abstract class OverlayBackingEntity {

    public abstract EntityReference entityReference();
    public abstract Long groupId();

}
