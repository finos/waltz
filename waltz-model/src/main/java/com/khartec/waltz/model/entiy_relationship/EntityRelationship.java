package com.khartec.waltz.model.entiy_relationship;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;

import static com.khartec.waltz.model.entiy_relationship.RelationshipKind.HAS;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityRelationship.class)
@JsonDeserialize(as = ImmutableEntityRelationship.class)
public abstract class EntityRelationship implements ProvenanceProvider {

    public abstract EntityReference a();
    public abstract EntityReference b();

    @Value.Default
    public RelationshipKind relationship() {
        return HAS;
    }

}
