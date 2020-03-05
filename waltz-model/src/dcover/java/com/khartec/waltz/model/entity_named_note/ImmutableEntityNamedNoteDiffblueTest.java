package com.khartec.waltz.model.entity_named_note;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableEntityNamedNoteDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityNamedNote.Json actualJson = new ImmutableEntityNamedNote.Json();

    // Assert
    assertNull(actualJson.provenance);
    assertFalse(actualJson.namedNoteTypeIdIsSet);
    assertEquals(0L, actualJson.namedNoteTypeId);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.noteText);
    assertNull(actualJson.lastUpdatedBy);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableEntityNamedNote.Json json = new ImmutableEntityNamedNote.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNamedNoteTypeIdTest() {
    // Arrange
    ImmutableEntityNamedNote.Json json = new ImmutableEntityNamedNote.Json();

    // Act
    json.setNamedNoteTypeId(123L);

    // Assert
    assertTrue(json.namedNoteTypeIdIsSet);
    assertEquals(123L, json.namedNoteTypeId);
  }
  @Test
  public void setNoteTextTest() {
    // Arrange
    ImmutableEntityNamedNote.Json json = new ImmutableEntityNamedNote.Json();

    // Act
    json.setNoteText("noteText");

    // Assert
    assertEquals("noteText", json.noteText);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableEntityNamedNote.Json json = new ImmutableEntityNamedNote.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

