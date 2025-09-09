package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.command.Command;
import org.finos.waltz.model.physical_flow.FlowAttributes;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowCommand.class)
@JsonDeserialize(as = ImmutableProposedFlowCommand.class)
public abstract class ProposedFlowCommand implements Command {
    public abstract EntityReference source();
    public abstract EntityReference target();
    public abstract Reason reason();
    public abstract Optional<Long> logicalFlowId();
    public abstract Optional<Long> physicalFlowId();
    public abstract PhysicalSpecification specification();
    public abstract FlowAttributes flowAttributes();
    @Value.Default
    public Set<Long> dataTypeIds() {
        return emptySet();
    }
}
