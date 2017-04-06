package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionSampleFile.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionSampleFile.class)
public abstract class PhysicalSpecDefinitionSampleFile implements IdProvider, NameProvider {

    public abstract long specDefinitionId();
    public abstract String fileData();
}
