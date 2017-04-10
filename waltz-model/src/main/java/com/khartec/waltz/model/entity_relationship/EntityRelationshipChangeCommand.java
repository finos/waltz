package com.khartec.waltz.model.entity_relationship;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableEntityRelationshipChangeCommand.class)
@JsonDeserialize(as = ImmutableEntityRelationshipChangeCommand.class)
public abstract class EntityRelationshipChangeCommand {

    public abstract Operation operation();
    public abstract EntityReference entityReference();
    public abstract RelationshipKind relationship();

}
