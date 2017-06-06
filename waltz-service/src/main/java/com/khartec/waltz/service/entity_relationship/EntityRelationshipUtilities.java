package com.khartec.waltz.service.entity_relationship;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.entity_relationship.EntityRelationshipKey;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationshipKey;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;


public class EntityRelationshipUtilities {

    public static Optional<EntityRelationshipKey> mkEntityRelationshipKey(EntityReference entityA,
                                                                       EntityReference entityB,
                                                                       RelationshipKind relationshipKind) {
        checkNotNull(relationshipKind, "relationshipKind cannot be null");
        checkNotNull(entityA, "entityA cannot be null");
        checkNotNull(entityB, "entityB cannot be null");

        // given A, B and a relationship kind -> return the valid entity relationship
        Set<Tuple2<EntityKind, EntityKind>> allowedEntityKinds = relationshipKind.getAllowedEntityKinds();

        Tuple2<EntityKind, EntityKind> exact = Tuple.tuple(entityA.kind(), entityB.kind());
        Tuple2<EntityKind, EntityKind> opposite = Tuple.tuple(entityB.kind(), entityA.kind());

        if (allowedEntityKinds.contains(exact)) {
            return Optional.of(ImmutableEntityRelationshipKey.builder()
                    .a(entityA)
                    .b(entityB)
                    .relationshipKind(relationshipKind)
                    .build());
        } else if (allowedEntityKinds.contains(opposite)){
            return Optional.of(ImmutableEntityRelationshipKey.builder()
                    .a(entityB)
                    .b(entityA)
                    .relationshipKind(relationshipKind)
                    .build());
        } else {
            return Optional.empty();
        }
    }


}
