package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@JsonSerialize(as = ImmutableBackingEntity.class)
@JsonDeserialize(as = ImmutableBackingEntity.class)
@Value.Immutable
public abstract class BackingEntity {

    public abstract EntityReference entityReference();
    public abstract String cellId();

}
