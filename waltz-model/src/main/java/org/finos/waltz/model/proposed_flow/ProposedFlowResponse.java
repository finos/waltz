package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.entity_workflow.EntityWorkflowState;
import org.finos.waltz.model.entity_workflow.EntityWorkflowTransition;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlowResponse.class)
@JsonDeserialize(as = ImmutableProposedFlowResponse.class)
public abstract class ProposedFlowResponse {

    public abstract Long id();

    public abstract LocalDateTime createdAt();

    public abstract String createdBy();

    public abstract ProposedFlowCommand flowDef();

    public abstract EntityWorkflowState workflowState();

    @Nullable
    public abstract List<EntityWorkflowTransition> workflowTransitionList();

    @Nullable
    public abstract Long logicalFlowId();

    @Nullable
    public abstract Long physicalFlowId();

    @Nullable
    public abstract Long specificationId();

}
