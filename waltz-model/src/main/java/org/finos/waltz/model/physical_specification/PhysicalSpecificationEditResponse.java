package org.finos.waltz.model.physical_specification;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.command.CommandOutcome;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalSpecificationEditResponse.class)
@JsonDeserialize(as = ImmutablePhysicalSpecificationEditResponse.class)
public abstract class PhysicalSpecificationEditResponse {

    @Value.Default
    public CommandOutcome outcome() {
        return CommandOutcome.SUCCESS;
    }

    public abstract Optional<String> message();

}
