package com.khartec.waltz.model.bookmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutableBookmarkDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableBookmark.Json actualJson = new ImmutableBookmark.Json();

    // Assert
    Optional<Long> optional = actualJson.id;
    assertSame(actualJson.url, optional);
    assertSame(optional, actualJson.url);
    assertSame(optional, actualJson.description);
    assertSame(optional, actualJson.title);
  }
  @Test
  public void setBookmarkKindTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setBookmarkKind("bookmarkKind");

    // Assert
    assertEquals("bookmarkKind", json.bookmarkKind);
  }
  @Test
  public void setIsPrimaryTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setIsPrimary(true);

    // Assert
    assertTrue(json.isPrimary);
    assertTrue(json.isPrimaryIsSet);
  }
  @Test
  public void setIsRequiredTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setIsRequired(true);

    // Assert
    assertTrue(json.isRequired);
    assertTrue(json.isRequiredIsSet);
  }
  @Test
  public void setIsRestrictedTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setIsRestricted(true);

    // Assert
    assertTrue(json.isRestricted);
    assertTrue(json.isRestrictedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableBookmark.Json json = new ImmutableBookmark.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

