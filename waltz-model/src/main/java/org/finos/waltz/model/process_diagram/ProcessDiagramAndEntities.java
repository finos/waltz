package org.finos.waltz.model.process_diagram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableProcessDiagramAndEntities.class)
public abstract class ProcessDiagramAndEntities  {

    public abstract ProcessDiagram diagram();
    public abstract Set<ProcessDiagramEntity> entities();
}
