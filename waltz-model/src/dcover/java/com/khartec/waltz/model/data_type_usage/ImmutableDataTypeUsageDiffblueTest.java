package com.khartec.waltz.model.data_type_usage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableDataTypeUsageDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDataTypeUsage.Json actualJson = new ImmutableDataTypeUsage.Json();

    // Assert
    assertNull(actualJson.dataTypeId);
    assertNull(actualJson.usage);
    assertNull(actualJson.entityReference);
    assertNull(actualJson.provenance);
  }



  @Test
  public void setDataTypeIdTest() {
    // Arrange
    ImmutableDataTypeUsage.Json json = new ImmutableDataTypeUsage.Json();

    // Act
    json.setDataTypeId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.dataTypeId);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableDataTypeUsage.Json json = new ImmutableDataTypeUsage.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

