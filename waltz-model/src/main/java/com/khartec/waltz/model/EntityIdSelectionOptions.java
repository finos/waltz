package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableEntityIdSelectionOptions.class)
public abstract class EntityIdSelectionOptions {
    public abstract EntityKind desiredKind();
    public abstract EntityReference entityReference();
    public abstract HierarchyQueryScope scope();
}
