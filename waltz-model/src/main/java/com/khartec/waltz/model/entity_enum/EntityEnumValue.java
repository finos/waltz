package com.khartec.waltz.model.entity_enum;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityEnumValue.class)
@JsonDeserialize(as = ImmutableEntityEnumValue.class)
public abstract class EntityEnumValue implements
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract long definitionId();
    public abstract EntityReference entityReference();
    public abstract String enumValueKey();
}
