package com.khartec.waltz.model.entity_workflow;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityWorkflowDefinition.class)
@JsonDeserialize(as = ImmutableEntityWorkflowDefinition.class)
public abstract class EntityWorkflowDefinition implements
        IdProvider,
        NameProvider,
        DescriptionProvider {
}
