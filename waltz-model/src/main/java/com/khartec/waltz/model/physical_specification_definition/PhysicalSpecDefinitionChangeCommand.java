package com.khartec.waltz.model.physical_specification_definition;

import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public abstract class PhysicalSpecDefinitionChangeCommand implements IdProvider {

    public abstract String version();
    public abstract Optional<String> delimiter();
    public abstract PhysicalSpecDefinitionType type();
}
