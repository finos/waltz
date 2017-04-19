package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionSampleFileCreateCommand.class)
public abstract class PhysicalSpecDefinitionSampleFileCreateCommand {

    public abstract String name();
    public abstract String fileData();

}
