package com.khartec.waltz.model.database_information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.AssetCodeProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Date;
import org.junit.Test;

public class ImmutableDatabaseInformationDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDatabaseInformation.Json actualJson = new ImmutableDatabaseInformation.Json();

    // Assert
    assertNull(actualJson.databaseName);
    assertNull(actualJson.endOfLifeDate);
    assertNull(actualJson.lifecycleStatus);
    assertNull(actualJson.environment);
    assertNull(actualJson.dbmsVendor);
    assertNull(actualJson.dbmsVersion);
    assertNull(actualJson.dbmsName);
    assertNull(actualJson.provenance);
    assertNull(actualJson.assetCode);
    assertNull(actualJson.instanceName);
  }
  @Test
  public void setAssetCodeTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setAssetCode("assetCode");

    // Assert
    assertEquals("assetCode", json.assetCode);
  }
  @Test
  public void setDatabaseNameTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setDatabaseName("databaseName");

    // Assert
    assertEquals("databaseName", json.databaseName);
  }
  @Test
  public void setDbmsNameTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setDbmsName("dbmsName");

    // Assert
    assertEquals("dbmsName", json.dbmsName);
  }
  @Test
  public void setDbmsVendorTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setDbmsVendor("dbmsVendor");

    // Assert
    assertEquals("dbmsVendor", json.dbmsVendor);
  }
  @Test
  public void setDbmsVersionTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setDbmsVersion("dbmsVersion");

    // Assert
    assertEquals("dbmsVersion", json.dbmsVersion);
  }
  @Test
  public void setEndOfLifeDateTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setEndOfLifeDate(date);

    // Assert
    assertSame(date, json.endOfLifeDate);
  }
  @Test
  public void setEnvironmentTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setEnvironment("environment");

    // Assert
    assertEquals("environment", json.environment);
  }
  @Test
  public void setInstanceNameTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setInstanceName("instanceName");

    // Assert
    assertEquals("instanceName", json.instanceName);
  }
  @Test
  public void setLifecycleStatusTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setLifecycleStatus(LifecycleStatus.ACTIVE);

    // Assert
    assertEquals(LifecycleStatus.ACTIVE, json.lifecycleStatus);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableDatabaseInformation.Json json = new ImmutableDatabaseInformation.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

