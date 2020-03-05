package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSurveyRunCreateCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyRunCreateCommand.Json actualJson = new ImmutableSurveyRunCreateCommand.Json();

    // Assert
    Optional<String> actualOptional = actualJson.contactEmail;
    Optional<LocalDate> expectedOptional = actualJson.dueDate;
    assertEquals(0, actualJson.involvementKindIds.size());
    assertSame(expectedOptional, actualOptional);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSurveyRunCreateCommand.Json json = new ImmutableSurveyRunCreateCommand.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setInvolvementKindIdsTest() {
    // Arrange
    ImmutableSurveyRunCreateCommand.Json json = new ImmutableSurveyRunCreateCommand.Json();
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
    ImmutableSurveyRunCreateCommand.Json json = new ImmutableSurveyRunCreateCommand.Json();

    // Act
    json.setIssuanceKind(SurveyIssuanceKind.GROUP);

    // Assert
    assertEquals(SurveyIssuanceKind.GROUP, json.issuanceKind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSurveyRunCreateCommand.Json json = new ImmutableSurveyRunCreateCommand.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setSurveyTemplateIdTest() {
    // Arrange
    ImmutableSurveyRunCreateCommand.Json json = new ImmutableSurveyRunCreateCommand.Json();

    // Act
    json.setSurveyTemplateId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyTemplateId);
  }
}

