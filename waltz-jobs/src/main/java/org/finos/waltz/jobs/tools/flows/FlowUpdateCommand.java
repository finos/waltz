package org.finos.waltz.jobs.tools.flows;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public abstract class FlowUpdateCommand {

    public abstract FlowUpdateCommandType action();
    public abstract EntityReference sourceEntityRef();
    public abstract EntityReference targetEntityRef();
    public abstract EntityReference dataTypeRef();
    @Nullable
    public abstract Long logicalFlowId();
}
