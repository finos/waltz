package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregateOverlayDiagram.class)
@JsonDeserialize(as = ImmutableAggregateOverlayDiagram.class)
public abstract class AggregateOverlayDiagram implements IdProvider, NameProvider, DescriptionProvider, LastUpdatedProvider, ProvenanceProvider {

    public abstract String svg();

}
