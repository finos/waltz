package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionChangeCommand.class)
public abstract class PhysicalSpecDefinitionChangeCommand implements IdProvider {

    public abstract String version();
    public abstract ReleaseLifecycleStatus status();
    public abstract Optional<String> delimiter();
    public abstract PhysicalSpecDefinitionType type();
}
