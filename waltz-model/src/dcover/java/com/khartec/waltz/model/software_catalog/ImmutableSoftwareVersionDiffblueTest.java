package com.khartec.waltz.model.software_catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.CreatedUserTimestampProvider;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSoftwareVersionDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwareVersion.Json actualJson = new ImmutableSoftwareVersion.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(optional, actualJson.externalId);
    assertSame(optional, actualJson.created);
  }









  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setKindTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setSoftwarePackageIdTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setSoftwarePackageId(123L);

    // Assert
    assertEquals(123L, json.softwarePackageId);
    assertTrue(json.softwarePackageIdIsSet);
  }

  @Test
  public void setVersionTest() {
    // Arrange
    ImmutableSoftwareVersion.Json json = new ImmutableSoftwareVersion.Json();

    // Act
    json.setVersion("version");

    // Assert
    assertEquals("version", json.version);
  }
}

