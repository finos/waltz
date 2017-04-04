package com.khartec.waltz.model.physical_specification_definition;

import org.immutables.value.Value;

@Value.Immutable
public abstract class PhysicalSpecDefinitionSampleFileCreateCommand {

    public abstract String name();
    public abstract String fileData();

}
