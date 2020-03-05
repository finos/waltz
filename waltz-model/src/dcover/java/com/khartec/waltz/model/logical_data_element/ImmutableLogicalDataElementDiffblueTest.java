package com.khartec.waltz.model.logical_data_element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.FieldDataType;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import org.junit.Test;

public class ImmutableLogicalDataElementDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLogicalDataElement.Json actualJson = new ImmutableLogicalDataElement.Json();

    // Assert
    assertSame(actualJson.id, actualJson.externalId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setParentDataTypeIdTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setParentDataTypeId(123L);

    // Assert
    assertEquals(123L, json.parentDataTypeId);
    assertTrue(json.parentDataTypeIdIsSet);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setTypeTest() {
    // Arrange
    ImmutableLogicalDataElement.Json json = new ImmutableLogicalDataElement.Json();

    // Act
    json.setType(FieldDataType.DATE);

    // Assert
    assertEquals(FieldDataType.DATE, json.type);
  }
}

