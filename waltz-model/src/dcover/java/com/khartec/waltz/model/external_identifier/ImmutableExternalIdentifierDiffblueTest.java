package com.khartec.waltz.model.external_identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.WaltzEntity;
import org.junit.Test;

public class ImmutableExternalIdentifierDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableExternalIdentifier.Json actualJson = new ImmutableExternalIdentifier.Json();

    // Assert
    assertNull(actualJson.externalId);
    assertNull(actualJson.system);
    assertNull(actualJson.entityReference);
  }
  @Test
  public void setExternalIdTest() {
    // Arrange
    ImmutableExternalIdentifier.Json json = new ImmutableExternalIdentifier.Json();

    // Act
    json.setExternalId("123");

    // Assert
    assertEquals("123", json.externalId);
  }
  @Test
  public void setSystemTest() {
    // Arrange
    ImmutableExternalIdentifier.Json json = new ImmutableExternalIdentifier.Json();

    // Act
    json.setSystem("system");

    // Assert
    assertEquals("system", json.system);
  }
}

