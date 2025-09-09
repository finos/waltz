package org.finos.waltz.model.entity_workflow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWorkflowView.class)
@JsonDeserialize(as = ImmutableEntityWorkflowView.class)
public abstract class EntityWorkflowView {
    public abstract EntityWorkflowDefinition workflowDefinition();

    public abstract EntityWorkflowState workflowState();

    public abstract List<EntityWorkflowTransition> workflowTransitionList();
}
