package com.khartec.waltz.model.physical_specification_data_type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutablePhysicalSpecificationDataTypeDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecificationDataType.Json actualJson = new ImmutablePhysicalSpecificationDataType.Json();

    // Assert
    assertEquals(0L, actualJson.dataTypeId);
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.provenance);
    assertFalse(actualJson.dataTypeIdIsSet);
    assertNull(actualJson.lastUpdatedBy);
    assertEquals(0L, actualJson.specificationId);
    assertFalse(actualJson.specificationIdIsSet);
  }
  @Test
  public void setDataTypeIdTest() {
    // Arrange
    ImmutablePhysicalSpecificationDataType.Json json = new ImmutablePhysicalSpecificationDataType.Json();

    // Act
    json.setDataTypeId(123L);

    // Assert
    assertEquals(123L, json.dataTypeId);
    assertTrue(json.dataTypeIdIsSet);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalSpecificationDataType.Json json = new ImmutablePhysicalSpecificationDataType.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutablePhysicalSpecificationDataType.Json json = new ImmutablePhysicalSpecificationDataType.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setSpecificationIdTest() {
    // Arrange
    ImmutablePhysicalSpecificationDataType.Json json = new ImmutablePhysicalSpecificationDataType.Json();

    // Act
    json.setSpecificationId(123L);

    // Assert
    assertEquals(123L, json.specificationId);
    assertTrue(json.specificationIdIsSet);
  }
}

