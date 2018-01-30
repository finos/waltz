/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
                                                                          RelationshipKind relationshipKind,
                                                                          boolean validate) {
        checkNotNull(relationshipKind, "relationshipKind cannot be null");
        checkNotNull(entityA, "entityA cannot be null");
        checkNotNull(entityB, "entityB cannot be null");

        if (! validate) {
            return Optional.of(ImmutableEntityRelationshipKey.builder()
                    .a(entityA)
                    .b(entityB)
                    .relationshipKind(relationshipKind)
                    .build());
        }

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
