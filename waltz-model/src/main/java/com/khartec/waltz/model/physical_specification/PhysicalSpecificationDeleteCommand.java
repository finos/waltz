package com.khartec.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecificationDeleteCommand.class)
@JsonDeserialize(as = ImmutablePhysicalSpecificationDeleteCommand.class)
public abstract class PhysicalSpecificationDeleteCommand implements Command {

    public abstract long specificationId();
}
