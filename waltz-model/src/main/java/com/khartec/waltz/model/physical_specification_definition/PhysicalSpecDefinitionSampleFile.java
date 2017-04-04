package com.khartec.waltz.model.physical_specification_definition;

import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
public abstract class PhysicalSpecDefinitionSampleFile implements IdProvider, NameProvider {

    public abstract long specDefinitionId();
    public abstract String fileData();
}
