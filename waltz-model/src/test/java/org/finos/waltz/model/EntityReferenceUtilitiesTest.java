package org.finos.waltz.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityReferenceUtilitiesTest {

    private final EntityReference ref1 = mkRef(EntityKind.APPLICATION, 1L);
    private final EntityReference ref2 = mkRef(EntityKind.APPLICATION, 2L);


    @Test
    public void indexById_happycase() {
        Set<EntityReference> refs = asSet(
                ref1,
                ref2);

        Map<Long, EntityReference> result = EntityReferenceUtilities.indexById(refs);

        assertNotNull(result, "indexById should give a result");
        assertEquals(2, result.size(), "resultant map should have 2 entries");
        assertEquals(ref1, result.get(1L));
        assertEquals(ref2, result.get(2L));
        assertNull(result.get(3L));
    }


    @Test
    public void indexById_empty() {

        Map<Long, EntityReference> result = EntityReferenceUtilities.indexById(new HashSet<>());

        assertNotNull(result, "indexById should give a result");
        assertTrue(result.isEmpty());
    }


    @Test()
    public void indexById_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> EntityReferenceUtilities.indexById(null));
    }


    @Test
    public void sameRef() {
        assertTrue(EntityReferenceUtilities.sameRef(Optional.of(ref1), ref1));
        assertTrue(EntityReferenceUtilities.sameRef(Optional.of(ref1), mkRef(EntityKind.APPLICATION, 1L)));
        assertTrue(EntityReferenceUtilities.sameRef(Optional.of(ref1), mkRef(EntityKind.APPLICATION, 1L, "with a name")));
        assertTrue(EntityReferenceUtilities.sameRef(Optional.of(ref1), mkRef(EntityKind.APPLICATION, 1L, "with a name", "with a desc")));
        assertTrue(EntityReferenceUtilities.sameRef(Optional.of(ref1), mkRef(EntityKind.APPLICATION, 1L, "with a name", "with a desc", "with an ext id")));

        assertFalse(EntityReferenceUtilities.sameRef(Optional.of(ref1), ref2));
        assertFalse(EntityReferenceUtilities.sameRef(Optional.empty(), ref1));
        assertFalse(EntityReferenceUtilities.sameRef(Optional.empty(), null));
        assertFalse(EntityReferenceUtilities.sameRef(Optional.of(ref1), null));


    }

}
