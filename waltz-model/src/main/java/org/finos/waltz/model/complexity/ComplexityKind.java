package org.finos.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityKind.class)
@JsonDeserialize(as = ImmutableComplexityKind.class)
public abstract class ComplexityKind implements IdProvider, NameProvider, DescriptionProvider, ExternalIdProvider, EntityKindProvider {

    public abstract boolean isDefault();

    @Value.Default
    public EntityKind kind() { return EntityKind.COMPLEXITY_KIND; }

}
