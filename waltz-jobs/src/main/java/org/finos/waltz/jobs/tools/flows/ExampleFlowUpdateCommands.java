package org.finos.waltz.jobs.tools.flows;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.EntityKind;

import java.util.Set;

import static org.finos.waltz.model.EntityReference.mkRef;

public class ExampleFlowUpdateCommands {

    // TODO: this needs a file parser rather than a hard coded list

    public static final Set<FlowUpdateCommand> commands = SetUtilities.asSet(
            ImmutableFlowUpdateCommand.builder()
                    .action(FlowUpdateCommandType.ADD)
                    .sourceEntityRef(mkRef(EntityKind.ACTOR, 88L, "Risk User"))
                    .targetEntityRef(mkRef(EntityKind.APPLICATION, 179L, "Risk App"))
                    .dataTypeRef(mkRef(EntityKind.DATA_TYPE, 321L, "Book Data"))
                    .logicalFlowId(58588L)
                    .build());

}
