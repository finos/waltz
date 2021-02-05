package com.khartec.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexity.class)
@JsonDeserialize(as = ImmutableComplexity.class)
public abstract class Complexity implements IdProvider, LastUpdatedProvider, ProvenanceProvider, EntityKindProvider {

    public abstract EntityReference entityReference();
    public abstract Long complexityKindId();
    public abstract BigDecimal score();

    @Value.Default
    public EntityKind kind() { return EntityKind.COMPLEXITY; }

}
