package com.khartec.waltz.model.involvement_kind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableInvolvementKindDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableInvolvementKind.Json actualJson = new ImmutableInvolvementKind.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.description);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.name);
  }






  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableInvolvementKind.Json json = new ImmutableInvolvementKind.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableInvolvementKind.Json json = new ImmutableInvolvementKind.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableInvolvementKind.Json json = new ImmutableInvolvementKind.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

