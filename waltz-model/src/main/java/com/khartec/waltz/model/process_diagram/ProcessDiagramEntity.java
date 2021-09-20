package com.khartec.waltz.model.process_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProcessDiagramEntity.class)
public abstract class ProcessDiagramEntity {

    public abstract Long diagramId();
    public abstract EntityReference entityReference();

    @Value.Default
    public boolean isNotable() {
        return false;
    }

}
