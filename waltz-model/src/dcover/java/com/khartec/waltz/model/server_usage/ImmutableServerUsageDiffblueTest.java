package com.khartec.waltz.model.server_usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableServerUsageDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableServerUsage.Json actualJson = new ImmutableServerUsage.Json();

    // Assert
    assertEquals(0L, actualJson.serverId);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.environment);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.serverIdIsSet);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.entityReference);
  }
  @Test
  public void setEnvironmentTest() {
    // Arrange
    ImmutableServerUsage.Json json = new ImmutableServerUsage.Json();

    // Act
    json.setEnvironment("environment");

    // Assert
    assertEquals("environment", json.environment);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableServerUsage.Json json = new ImmutableServerUsage.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableServerUsage.Json json = new ImmutableServerUsage.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setServerIdTest() {
    // Arrange
    ImmutableServerUsage.Json json = new ImmutableServerUsage.Json();

    // Act
    json.setServerId(123L);

    // Assert
    assertEquals(123L, json.serverId);
    assertTrue(json.serverIdIsSet);
  }
}

