package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import org.junit.Test;

public class ImmutableSurveyTemplateDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyTemplate.Json actualJson = new ImmutableSurveyTemplate.Json();

    // Assert
    assertNull(actualJson.createdAt);
    assertNull(actualJson.name);
    assertNull(actualJson.description);
    assertNull(actualJson.ownerId);
    assertNull(actualJson.targetEntityKind);
    assertNull(actualJson.status);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSurveyTemplate.Json json = new ImmutableSurveyTemplate.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSurveyTemplate.Json json = new ImmutableSurveyTemplate.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOwnerIdTest() {
    // Arrange
    ImmutableSurveyTemplate.Json json = new ImmutableSurveyTemplate.Json();

    // Act
    json.setOwnerId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.ownerId);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableSurveyTemplate.Json json = new ImmutableSurveyTemplate.Json();

    // Act
    json.setStatus(ReleaseLifecycleStatus.DRAFT);

    // Assert
    assertEquals(ReleaseLifecycleStatus.DRAFT, json.status);
  }
  @Test
  public void setTargetEntityKindTest() {
    // Arrange
    ImmutableSurveyTemplate.Json json = new ImmutableSurveyTemplate.Json();

    // Act
    json.setTargetEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.targetEntityKind);
  }
}

