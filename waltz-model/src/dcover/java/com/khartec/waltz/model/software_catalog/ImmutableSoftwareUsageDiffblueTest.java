package com.khartec.waltz.model.software_catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableSoftwareUsageDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwareUsage.Json actualJson = new ImmutableSoftwareUsage.Json();

    // Assert
    assertEquals(0L, actualJson.softwareVersionId);
    assertNull(actualJson.licenceId);
    assertEquals(0L, actualJson.softwarePackageId);
    assertFalse(actualJson.softwarePackageIdIsSet);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.applicationIdIsSet);
    assertEquals(0L, actualJson.applicationId);
    assertFalse(actualJson.softwareVersionIdIsSet);
  }
  @Test
  public void setApplicationIdTest() {
    // Arrange
    ImmutableSoftwareUsage.Json json = new ImmutableSoftwareUsage.Json();

    // Act
    json.setApplicationId(123L);

    // Assert
    assertTrue(json.applicationIdIsSet);
    assertEquals(123L, json.applicationId);
  }
  @Test
  public void setLicenceIdTest() {
    // Arrange
    ImmutableSoftwareUsage.Json json = new ImmutableSoftwareUsage.Json();

    // Act
    json.setLicenceId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.licenceId);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSoftwareUsage.Json json = new ImmutableSoftwareUsage.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setSoftwarePackageIdTest() {
    // Arrange
    ImmutableSoftwareUsage.Json json = new ImmutableSoftwareUsage.Json();

    // Act
    json.setSoftwarePackageId(123L);

    // Assert
    assertEquals(123L, json.softwarePackageId);
    assertTrue(json.softwarePackageIdIsSet);
  }
  @Test
  public void setSoftwareVersionIdTest() {
    // Arrange
    ImmutableSoftwareUsage.Json json = new ImmutableSoftwareUsage.Json();

    // Act
    json.setSoftwareVersionId(123L);

    // Assert
    assertEquals(123L, json.softwareVersionId);
    assertTrue(json.softwareVersionIdIsSet);
  }
}

