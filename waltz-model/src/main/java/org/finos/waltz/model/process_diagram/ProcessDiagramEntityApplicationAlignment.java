package org.finos.waltz.model.process_diagram;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableProcessDiagramEntityApplicationAlignment.class)
@JsonDeserialize(as = ImmutableProcessDiagramEntityApplicationAlignment.class)
public abstract class ProcessDiagramEntityApplicationAlignment {


    /**
     * Reference to the measurable shown on the diagram
     */
    public abstract EntityReference diagramMeasurableRef();

    /**
     * Reference to the application that rolls up to the diagram entity
     */
    public abstract EntityReference applicationRef();

    /**
     * Reference to the measurable which links the application via the measurable hierarchy to the diagram entity (i.e. child or self)
     */
    public abstract EntityReference referencedMeasurableRef();

}
