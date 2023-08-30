package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.command.Command;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableOverlayDiagramSaveCommand.class)
@JsonDeserialize(as = ImmutableOverlayDiagramSaveCommand.class)
public abstract class OverlayDiagramSaveCommand implements IdProvider, NameProvider, DescriptionProvider, Command {

    public abstract String layoutData();
    public abstract EntityKind aggregatedEntityKind();
    public abstract OverlayDiagramKind diagramKind();
    public abstract Set<BackingEntity> backingEntities();

}
