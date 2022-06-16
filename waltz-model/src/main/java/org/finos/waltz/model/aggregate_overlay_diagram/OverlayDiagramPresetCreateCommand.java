package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramPresetCreateCommand.class)
@JsonDeserialize(as = ImmutableOverlayDiagramPresetCreateCommand.class)
public abstract class OverlayDiagramPresetCreateCommand implements NameProvider, DescriptionProvider, Command {

    public abstract Long diagramId();

    public abstract String externalId();

    public abstract String overlayConfig();

    public abstract String filterConfig();

}
