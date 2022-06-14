package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregateOverlayDiagramPreset.class)
@JsonDeserialize(as = ImmutableAggregateOverlayDiagramPreset.class)
public abstract class AggregateOverlayDiagramPreset implements IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract Long diagramId();

    public abstract String externalId();

    public abstract String overlayConfig();

    public abstract String filterConfig();

}
