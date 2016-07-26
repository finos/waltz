package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticDefinition.class)
@JsonDeserialize(as = ImmutableEntityStatisticDefinition.class)
public abstract class EntityStatisticDefinition implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        ParentIdProvider
{

    public abstract StatisticType type();
    public abstract StatisticCategory category();
    public abstract boolean active();
    public abstract String renderer();
    public abstract String historicRenderer();
}
