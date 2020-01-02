/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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
