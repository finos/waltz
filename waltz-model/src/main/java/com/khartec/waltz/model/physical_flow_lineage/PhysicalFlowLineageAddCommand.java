package com.khartec.waltz.model.physical_flow_lineage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.command.Command;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowLineageAddCommand.class)
@JsonDeserialize(as = ImmutablePhysicalFlowLineageAddCommand.class)
public abstract class PhysicalFlowLineageAddCommand implements Command, DescriptionProvider {

    public abstract long describedFlowId();
    public abstract long contributingFlowId();
    public abstract LastUpdate lastUpdate();
}
