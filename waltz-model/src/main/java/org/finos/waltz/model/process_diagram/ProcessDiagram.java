package org.finos.waltz.model.process_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableProcessDiagram.class)
public abstract class ProcessDiagram implements
        IdProvider,
        EntityKindProvider,
        NameProvider,
        ExternalIdProvider,
        ProvenanceProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        CreatedProvider {

    public abstract ProcessDiagramKind diagramKind();
    public abstract Optional<String> layoutData();

    @Value.Default
    public EntityKind kind() {
        return EntityKind.PROCESS_DIAGRAM;
    }
}
