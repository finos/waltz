package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableSurveyTemplateChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyTemplateChangeCommand.Json actualJson = new ImmutableSurveyTemplateChangeCommand.Json();

    // Assert
    assertNull(actualJson.targetEntityKind);
    assertNull(actualJson.name);
    assertNull(actualJson.description);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSurveyTemplateChangeCommand.Json json = new ImmutableSurveyTemplateChangeCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSurveyTemplateChangeCommand.Json json = new ImmutableSurveyTemplateChangeCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setTargetEntityKindTest() {
    // Arrange
    ImmutableSurveyTemplateChangeCommand.Json json = new ImmutableSurveyTemplateChangeCommand.Json();

    // Act
    json.setTargetEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.targetEntityKind);
  }
}

