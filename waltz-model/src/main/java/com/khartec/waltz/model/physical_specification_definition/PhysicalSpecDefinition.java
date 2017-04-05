package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinition.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinition.class)
public abstract class PhysicalSpecDefinition implements
        IdProvider,
        CreatedProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract long specificationId();
    public abstract String version();
    public abstract Optional<String> delimiter();
    public abstract PhysicalSpecDefinitionType type();
    public abstract ReleaseLifecycleStatus status();

}
