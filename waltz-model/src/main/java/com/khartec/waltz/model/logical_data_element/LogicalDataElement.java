package com.khartec.waltz.model.logical_data_element;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

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
    public abstract long parentDataTypeId();

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
