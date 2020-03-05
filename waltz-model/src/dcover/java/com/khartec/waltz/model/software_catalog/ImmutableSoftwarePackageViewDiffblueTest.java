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

public class ImmutableSoftwarePackageViewDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwarePackageView.Json actualJson = new ImmutableSoftwarePackageView.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(actualJson.created, optional);
    assertSame(optional, actualJson.created);
    assertSame(optional, actualJson.externalId);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setGroupTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setGroup("group");

    // Assert
    assertEquals("group", json.group);
  }
  @Test
  public void setIsNotableTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setIsNotable(true);

    // Assert
    assertTrue(json.isNotable);
    assertTrue(json.isNotableIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
  @Test
  public void setVendorTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setVendor("vendor");

    // Assert
    assertEquals("vendor", json.vendor);
  }
  @Test
  public void setVersionTest() {
    // Arrange
    ImmutableSoftwarePackageView.Json json = new ImmutableSoftwarePackageView.Json();

    // Act
    json.setVersion("version");

    // Assert
    assertEquals("version", json.version);
  }
}

