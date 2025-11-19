package org.finos.waltz.model.entity_workflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.CreatedProvider;
import org.finos.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWorkflowResult.class)
@JsonDeserialize(as = ImmutableEntityWorkflowResult.class)
public abstract class EntityWorkflowResult implements CreatedProvider {
    public abstract long workflowId();

    public abstract EntityReference workflowEntity();

    public abstract EntityReference resultEntity();
}
