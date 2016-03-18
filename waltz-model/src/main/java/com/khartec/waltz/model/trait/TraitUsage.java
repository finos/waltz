package com.khartec.waltz.model.trait;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableTraitUsage.class)
@JsonDeserialize(as = ImmutableTraitUsage.class)
public abstract class TraitUsage {

    public abstract long traitId();

    public abstract EntityReference entityReference();

    public abstract TraitUsageKind relationship();
}
