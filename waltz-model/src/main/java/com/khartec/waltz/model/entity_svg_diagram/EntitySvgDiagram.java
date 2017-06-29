package com.khartec.waltz.model.entity_svg_diagram;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntitySvgDiagram.class)
@JsonDeserialize(as = ImmutableEntitySvgDiagram.class)
public abstract class EntitySvgDiagram implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        ExternalIdProvider {

    public abstract String svg();
    public abstract EntityReference entityReference();
}
