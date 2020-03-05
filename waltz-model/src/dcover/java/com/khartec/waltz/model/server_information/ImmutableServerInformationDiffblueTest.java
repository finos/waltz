package com.khartec.waltz.model.server_information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Date;
import org.junit.Test;

public class ImmutableServerInformationDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableServerInformation.Json actualJson = new ImmutableServerInformation.Json();

    // Assert
    assertSame(actualJson.externalId, actualJson.id);
  }
  @Test
  public void setCountryTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setCountry("country");

    // Assert
    assertEquals("country", json.country);
  }
  @Test
  public void setHardwareEndOfLifeDateTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setHardwareEndOfLifeDate(date);

    // Assert
    assertSame(date, json.hardwareEndOfLifeDate);
  }
  @Test
  public void setHostnameTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setHostname("localhost");

    // Assert
    assertEquals("localhost", json.hostname);
  }
  @Test
  public void setLifecycleStatusTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setLifecycleStatus(LifecycleStatus.ACTIVE);

    // Assert
    assertEquals(LifecycleStatus.ACTIVE, json.lifecycleStatus);
  }
  @Test
  public void setLocationTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setLocation("location");

    // Assert
    assertEquals("location", json.location);
  }
  @Test
  public void setOperatingSystemEndOfLifeDateTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setOperatingSystemEndOfLifeDate(date);

    // Assert
    assertSame(date, json.operatingSystemEndOfLifeDate);
  }
  @Test
  public void setOperatingSystemTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setOperatingSystem("operatingSystem");

    // Assert
    assertEquals("operatingSystem", json.operatingSystem);
  }
  @Test
  public void setOperatingSystemVersionTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setOperatingSystemVersion("operatingSystemVersion");

    // Assert
    assertEquals("operatingSystemVersion", json.operatingSystemVersion);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setVirtualTest() {
    // Arrange
    ImmutableServerInformation.Json json = new ImmutableServerInformation.Json();

    // Act
    json.setVirtual(true);

    // Assert
    assertTrue(json.virtual);
    assertTrue(json.virtualIsSet);
  }
}

