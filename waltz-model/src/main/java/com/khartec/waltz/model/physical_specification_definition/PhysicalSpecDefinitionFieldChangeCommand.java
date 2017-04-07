package com.khartec.waltz.model.physical_specification_definition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecDefinitionFieldChangeCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecDefinitionFieldChangeCommand.class)
public abstract class PhysicalSpecDefinitionFieldChangeCommand implements
        IdProvider,
        NameProvider,
        DescriptionProvider {

    public abstract int position();
    public abstract PhysicalSpecDefinitionFieldType type();

}
