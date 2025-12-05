package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableDeletePhysicalFlowResponse.class)
@JsonDeserialize(as = ImmutableDeletePhysicalFlowResponse.class)
public abstract class DeletePhysicalFlowResponse {

    public abstract Long logicalFlowId();

    public abstract Long physicalFlowId();

    public abstract Long specificationId();
}
