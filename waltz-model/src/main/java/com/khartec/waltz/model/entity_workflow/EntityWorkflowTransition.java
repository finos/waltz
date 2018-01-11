package com.khartec.waltz.model.entity_workflow;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWorkflowTransition.class)
@JsonDeserialize(as = ImmutableEntityWorkflowTransition.class)
public abstract class EntityWorkflowTransition implements
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract long workflowId();
    public abstract EntityReference entityReference();

    @Nullable
    public abstract String fromState();
    public abstract String toState();

    @Nullable
    public abstract String reason();
}
