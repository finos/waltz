package com.khartec.waltz.model.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.rating.RagRating;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;

public class ImmutableAppRegistrationRequestDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppRegistrationRequest.Json actualJson = new ImmutableAppRegistrationRequest.Json();

    // Assert
    Set<String> actualStringSet = actualJson.tags;
    Optional<String> optional = actualJson.provenance;
    assertSame(optional, actualJson.description);
    assertSame(optional, actualJson.assetCode);
    assertSame(optional, actualJson.parentAssetCode);
    Set<String> stringSet = actualJson.aliases;
    assertEquals(0, stringSet.size());
    assertSame(stringSet, actualStringSet);
  }
  @Test
  public void setAliasesTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();
    HashSet<String> stringSet = new HashSet<String>();
    stringSet.add("foo");

    // Act
    json.setAliases(stringSet);

    // Assert
    assertSame(stringSet, json.aliases);
  }
  @Test
  public void setApplicationKindTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setApplicationKind(ApplicationKind.IN_HOUSE);

    // Assert
    assertEquals(ApplicationKind.IN_HOUSE, json.applicationKind);
  }
  @Test
  public void setBusinessCriticalityTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setBusinessCriticality(Criticality.LOW);

    // Assert
    assertEquals(Criticality.LOW, json.businessCriticality);
  }
  @Test
  public void setLifecyclePhaseTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setLifecyclePhase(LifecyclePhase.PRODUCTION);

    // Assert
    assertEquals(LifecyclePhase.PRODUCTION, json.lifecyclePhase);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOrganisationalUnitIdTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setOrganisationalUnitId(123L);

    // Assert
    assertTrue(json.organisationalUnitIdIsSet);
    assertEquals(123L, json.organisationalUnitId);
  }
  @Test
  public void setOverallRatingTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();

    // Act
    json.setOverallRating(RagRating.R);

    // Assert
    assertEquals(RagRating.R, json.overallRating);
  }
  @Test
  public void setTagsTest() {
    // Arrange
    ImmutableAppRegistrationRequest.Json json = new ImmutableAppRegistrationRequest.Json();
    HashSet<String> stringSet = new HashSet<String>();
    stringSet.add("foo");

    // Act
    json.setTags(stringSet);

    // Assert
    assertSame(stringSet, json.tags);
  }
}

