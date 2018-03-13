package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.command.AbstractCommandResponse;
import org.immutables.value.Value;

import java.util.Map;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowUploadCommandResponse.class)
@JsonDeserialize(as = ImmutablePhysicalFlowUploadCommandResponse.class)
public abstract class PhysicalFlowUploadCommandResponse
        extends AbstractCommandResponse<PhysicalFlowUploadCommand> {

    // overridden as nullable because it is not used here
    @Nullable
    public abstract EntityReference entityReference();

    public abstract PhysicalFlowParsed parsedFlow();

    public abstract Map<String, String> errors();
}
