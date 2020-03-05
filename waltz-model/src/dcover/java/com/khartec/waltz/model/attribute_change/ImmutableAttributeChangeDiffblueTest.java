package com.khartec.waltz.model.attribute_change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableAttributeChangeDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAttributeChange.Json actualJson = new ImmutableAttributeChange.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.oldValue);
    assertEquals(0L, actualJson.changeUnitId);
    assertNull(actualJson.newValue);
    assertFalse(actualJson.changeUnitIdIsSet);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.type);
    assertNull(actualJson.name);
  }
  @Test
  public void setChangeUnitIdTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setChangeUnitId(123L);

    // Assert
    assertEquals(123L, json.changeUnitId);
    assertTrue(json.changeUnitIdIsSet);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setNewValueTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setNewValue("newValue");

    // Assert
    assertEquals("newValue", json.newValue);
  }
  @Test
  public void setOldValueTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setOldValue("oldValue");

    // Assert
    assertEquals("oldValue", json.oldValue);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutableAttributeChange.Json json = new ImmutableAttributeChange.Json();

    // Act
    json.setType("type");

    // Assert
    assertEquals("type", json.type);
  }
}

