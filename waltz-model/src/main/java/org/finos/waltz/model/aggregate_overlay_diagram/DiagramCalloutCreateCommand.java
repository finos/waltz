package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDiagramCalloutCreateCommand.class)
@JsonDeserialize(as = ImmutableDiagramCalloutCreateCommand.class)
public abstract class DiagramCalloutCreateCommand implements Command {

    public abstract Long instanceId();

    public abstract String cellExternalId();

    public abstract String title();

    public abstract String content();

    public abstract String startColor();

    public abstract String endColor();

}
