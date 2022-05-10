package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramInstanceCreateCommand.class)
@JsonDeserialize(as = ImmutableOverlayDiagramInstanceCreateCommand.class)
public abstract class OverlayDiagramInstanceCreateCommand implements NameProvider, DescriptionProvider, Command {

    public abstract Long diagramId();

    public abstract EntityReference parentEntityReference();

    public abstract String svg();
}
