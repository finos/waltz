package org.finos.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Criticality;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowInfo.class)
@JsonDeserialize(as = ImmutablePhysicalFlowInfo.class)
public abstract class PhysicalFlowInfo {

    public abstract EntityReference source();
    public abstract EntityReference target();

    public abstract EntityReference specification();

    public abstract EntityReference logicalFlow();

    @Nullable
    public abstract String physicalFlowExternalId();

    @Nullable
    public abstract String physicalFlowDescription();

    public abstract TransportKindValue transportKindValue();

    public abstract FrequencyKindValue frequencyKind();

    public abstract CriticalityValue criticality();

    public abstract List<EntityReference> dataTypes();

}
