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
