package com.khartec.waltz.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableIdSelectionOptions.class)
public abstract class IdSelectionOptions {

    public abstract EntityReference entityReference();
    public abstract HierarchyQueryScope scope();

    public static IdSelectionOptions mkOpts(EntityReference ref, HierarchyQueryScope scope) {
        return ImmutableIdSelectionOptions.builder()
                .entityReference(ref)
                .scope(scope)
                .build();
    }
}
