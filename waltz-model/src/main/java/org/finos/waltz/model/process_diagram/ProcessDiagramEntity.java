package org.finos.waltz.model.process_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
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
