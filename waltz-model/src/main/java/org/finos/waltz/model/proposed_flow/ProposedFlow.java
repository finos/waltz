package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableProposedFlow.class)
@JsonDeserialize(as = ImmutableProposedFlow.class)
public abstract class ProposedFlow {

    public abstract Long id();
    public abstract Long sourceEntityId();
    public abstract String sourceEntityKind();
    public abstract Long targetEntityId();
    public abstract String targetEntityKind();
    public abstract LocalDateTime createdAt();
    public abstract String createdBy();
    public abstract String flowDef();
}
