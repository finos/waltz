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

    public abstract Long sourceEntityId();

    public abstract String sourceEntityKind();

    public abstract Long targetEntityId();

    public abstract String targetEntityKind();

    public abstract LocalDateTime createdAt();

    public abstract String createdBy();

    public abstract ProposedFlowCommand flowDef();

    @Nullable
    public abstract Long logicalFlowId();

    @Nullable
    public abstract Long physicalFlowId();

    public abstract EntityWorkflowState workflowState();

    @Nullable
    public abstract List<EntityWorkflowTransition> workflowTransitionList();

}
