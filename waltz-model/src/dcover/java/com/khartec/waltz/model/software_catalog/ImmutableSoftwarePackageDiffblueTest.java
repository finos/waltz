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
import com.khartec.waltz.model.UserTimestamp;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSoftwarePackageDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwarePackage.Json actualJson = new ImmutableSoftwarePackage.Json();

    // Assert
    Optional<UserTimestamp> optional = actualJson.created;
    assertSame(actualJson.id, optional);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.externalId);
  }









  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setGroupTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setGroup("group");

    // Assert
    assertEquals("group", json.group);
  }

  @Test
  public void setIsNotableTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setIsNotable(true);

    // Assert
    assertTrue(json.isNotable);
    assertTrue(json.isNotableIsSet);
  }

  @Test
  public void setKindTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setVendorTest() {
    // Arrange
    ImmutableSoftwarePackage.Json json = new ImmutableSoftwarePackage.Json();

    // Act
    json.setVendor("vendor");

    // Assert
    assertEquals("vendor", json.vendor);
  }
}

