package com.khartec.waltz.model.orphan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ImmutableOrphanRelationshipDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableOrphanRelationship.Json actualJson = new ImmutableOrphanRelationship.Json();

    // Assert
    assertNull(actualJson.entityB);
    assertNull(actualJson.orphanSide);
    assertNull(actualJson.entityA);
  }
  @Test
  public void setOrphanSideTest() {
    // Arrange
    ImmutableOrphanRelationship.Json json = new ImmutableOrphanRelationship.Json();

    // Act
    json.setOrphanSide(OrphanSide.A);

    // Assert
    assertEquals(OrphanSide.A, json.orphanSide);
  }
}

