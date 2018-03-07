package com.khartec.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecificationAddCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecificationAddCommand.class)
public abstract class PhysicalSpecificationAddCommand implements
        Command,
        ExternalIdProvider,
        NameProvider,
        DescriptionProvider {

    public abstract EntityReference owningEntity();
    public abstract DataFormatKind format();
}
