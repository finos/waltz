package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSurveyRunChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyRunChangeCommand.Json actualJson = new ImmutableSurveyRunChangeCommand.Json();

    // Assert
    Optional<String> optional = actualJson.contactEmail;
    assertSame(actualJson.dueDate, optional);
    assertSame(optional, actualJson.dueDate);
    assertEquals(0, actualJson.involvementKindIds.size());
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSurveyRunChangeCommand.Json json = new ImmutableSurveyRunChangeCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setInvolvementKindIdsTest() {
    // Arrange
    ImmutableSurveyRunChangeCommand.Json json = new ImmutableSurveyRunChangeCommand.Json();
    HashSet<Long> resultLongSet = new HashSet<Long>();
    resultLongSet.add(1L);

    // Act
    json.setInvolvementKindIds(resultLongSet);

    // Assert
    assertSame(resultLongSet, json.involvementKindIds);
  }
  @Test
  public void setIssuanceKindTest() {
    // Arrange
    ImmutableSurveyRunChangeCommand.Json json = new ImmutableSurveyRunChangeCommand.Json();

    // Act
    json.setIssuanceKind(SurveyIssuanceKind.GROUP);

    // Assert
    assertEquals(SurveyIssuanceKind.GROUP, json.issuanceKind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSurveyRunChangeCommand.Json json = new ImmutableSurveyRunChangeCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setSurveyTemplateIdTest() {
    // Arrange
    ImmutableSurveyRunChangeCommand.Json json = new ImmutableSurveyRunChangeCommand.Json();

    // Act
    json.setSurveyTemplateId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyTemplateId);
  }
}

