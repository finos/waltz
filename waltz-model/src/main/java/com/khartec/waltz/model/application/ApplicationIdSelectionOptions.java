package com.khartec.waltz.model.application;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableApplicationIdSelectionOptions.class)
@JsonDeserialize(as = ImmutableApplicationIdSelectionOptions.class)
public abstract class ApplicationIdSelectionOptions {

    public abstract EntityReference entityReference();
    public abstract HierarchyQueryScope scope();

}
