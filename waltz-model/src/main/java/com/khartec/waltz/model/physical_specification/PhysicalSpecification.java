package com.khartec.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

/**
 * Represents something which is produced by an owning system.
 * This may be a message, file, document or similar.
 * The specification is discrete from the physical
 * flow to better represent systems which distribute a
 * single file to many consumers
 */
@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecification.class)
@JsonDeserialize(as = ImmutablePhysicalSpecification.class)
public abstract class PhysicalSpecification implements
        IdProvider,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider,
        ProvenanceProvider {

    public abstract long owningApplicationId();
    public abstract DataFormatKind format();

}
