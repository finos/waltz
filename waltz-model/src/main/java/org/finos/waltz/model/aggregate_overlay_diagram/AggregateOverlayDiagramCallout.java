package org.finos.waltz.model.aggregate_overlay_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.LastUpdatedProvider;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableAggregateOverlayDiagramCallout.class)
@JsonDeserialize(as = ImmutableAggregateOverlayDiagramCallout.class)
public abstract class AggregateOverlayDiagramCallout implements IdProvider {

    public abstract Long diagramInstanceId();

    public abstract String cellExternalId();

    public abstract String title();

    public abstract String content();

    public abstract String startColor();

    public abstract String endColor();

}
