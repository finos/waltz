package com.khartec.waltz.model.entity_workflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWorkflowState.class)
@JsonDeserialize(as = ImmutableEntityWorkflowState.class)
public abstract class EntityWorkflowState implements
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract long workflowId();
    public abstract EntityReference entityReference();
    public abstract String state();
}
