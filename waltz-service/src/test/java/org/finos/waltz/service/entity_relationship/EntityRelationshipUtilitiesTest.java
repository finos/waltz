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

package org.finos.waltz.service.entity_relationship;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.entity_relationship.EntityRelationshipKey;
import org.finos.waltz.model.entity_relationship.RelationshipKind;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.service.entity_relationship.EntityRelationshipUtilities.mkEntityRelationshipKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityRelationshipUtilitiesTest {

    private final EntityReference app = mkRef(EntityKind.APPLICATION, 1L);
    private final EntityReference ci = mkRef(EntityKind.CHANGE_INITIATIVE, 2L);
    private final EntityReference person = mkRef(EntityKind.PERSON, 3L);


    @Test
    public void nullArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> mkEntityRelationshipKey(null, ci, RelationshipKind.PARTICIPATES_IN, true));
        assertThrows(IllegalArgumentException.class, () -> mkEntityRelationshipKey(app, null, RelationshipKind.PARTICIPATES_IN, true));
        assertThrows(IllegalArgumentException.class, () -> mkEntityRelationshipKey(app, ci, null, true));
    }


    @Test
    public void withoutValidationKeyIsCreatedAsGiven() {
        Optional<EntityRelationshipKey> key = mkEntityRelationshipKey(person, app, RelationshipKind.PARTICIPATES_IN, false);

        assertTrue(key.isPresent());
        assertEquals(person, key.get().a());
        assertEquals(app, key.get().b());
        assertEquals("PARTICIPATES_IN", key.get().relationshipKind());
    }


    @Test
    public void exactMatchPreservesOrder() {
        // PARTICIPATES_IN allows (APPLICATION -> CHANGE_INITIATIVE)
        Optional<EntityRelationshipKey> key = mkEntityRelationshipKey(app, ci, RelationshipKind.PARTICIPATES_IN, true);

        assertTrue(key.isPresent());
        assertEquals(app, key.get().a());
        assertEquals(ci, key.get().b());
        assertEquals(RelationshipKind.PARTICIPATES_IN.name(), key.get().relationshipKind());
    }


    @Test
    public void oppositeMatchFlipsOrder() {
        Optional<EntityRelationshipKey> key = mkEntityRelationshipKey(ci, app, RelationshipKind.PARTICIPATES_IN, true);

        assertTrue(key.isPresent());
        assertEquals(app, key.get().a());
        assertEquals(ci, key.get().b());
    }


    @Test
    public void disallowedKindsGiveEmpty() {
        Optional<EntityRelationshipKey> key = mkEntityRelationshipKey(person, app, RelationshipKind.PARTICIPATES_IN, true);

        assertFalse(key.isPresent());
    }


    @Test
    public void kindWithNoAllowedEntitiesGivesEmptyWhenValidating() {
        assertFalse(mkEntityRelationshipKey(app, ci, RelationshipKind.HAS, true).isPresent());
        assertTrue(mkEntityRelationshipKey(app, ci, RelationshipKind.HAS, false).isPresent());
    }

}
