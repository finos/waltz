package com.khartec.waltz.model.physical_specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CreatedUserTimestampProvider;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.IsRemovedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.UserTimestamp;
import java.util.Optional;
import org.junit.Test;

public class ImmutablePhysicalSpecificationDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePhysicalSpecification.Json actualJson = new ImmutablePhysicalSpecification.Json();

    // Assert
    Optional<UserTimestamp> optional = actualJson.created;
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setFormatTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setFormat(DataFormatKind.BINARY);

    // Assert
    assertEquals(DataFormatKind.BINARY, json.format);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemovedIsSet);
    assertTrue(json.isRemoved);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutablePhysicalSpecification.Json json = new ImmutablePhysicalSpecification.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

