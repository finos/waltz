package com.khartec.waltz.model.logical_data_element;

import com.khartec.waltz.model.*;
import com.khartec.waltz.model.FieldDataType;
import org.immutables.value.Value;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableLogicalDataElement.class)
@JsonDeserialize(as = ImmutableLogicalDataElement.class)
public abstract class LogicalDataElement implements
        IdProvider,
        EntityKindProvider,
        EntityLifecycleStatusProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider,
        WaltzEntity {

    @Value.Default
    public EntityKind kind() { return EntityKind.LOGICAL_DATA_ELEMENT; }


    public abstract FieldDataType type();


    @Value.Default
    public String provenance() {
        return "waltz";
    }


    public EntityReference entityReference() {
        return ImmutableEntityReference.builder()
                .kind(EntityKind.LOGICAL_DATA_ELEMENT)
                .id(id().get())
                .name(name())
                .description(description())
                .entityLifecycleStatus(entityLifecycleStatus())
                .build();
    }
}
