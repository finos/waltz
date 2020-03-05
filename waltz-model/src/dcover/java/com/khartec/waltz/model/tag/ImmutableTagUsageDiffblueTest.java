package com.khartec.waltz.model.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.WaltzEntity;
import org.junit.Test;

public class ImmutableTagUsageDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableTagUsage.Json actualJson = new ImmutableTagUsage.Json();

    // Assert
    assertNull(actualJson.createdBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.createdAt);
    assertFalse(actualJson.tagIdIsSet);
    assertNull(actualJson.entityReference);
    assertEquals(0L, actualJson.tagId);
  }





  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutableTagUsage.Json json = new ImmutableTagUsage.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableTagUsage.Json json = new ImmutableTagUsage.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setTagIdTest() {
    // Arrange
    ImmutableTagUsage.Json json = new ImmutableTagUsage.Json();

    // Act
    json.setTagId(123L);

    // Assert
    assertTrue(json.tagIdIsSet);
    assertEquals(123L, json.tagId);
  }
}

