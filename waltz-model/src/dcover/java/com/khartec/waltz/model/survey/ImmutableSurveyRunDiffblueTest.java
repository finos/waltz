package com.khartec.waltz.model.survey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.NameProvider;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import org.junit.Test;

public class ImmutableSurveyRunDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSurveyRun.Json actualJson = new ImmutableSurveyRun.Json();

    // Assert
    Optional<Long> actualOptional = actualJson.id;
    Optional<LocalDate> expectedOptional = actualJson.issuedOn;
    assertEquals(0, actualJson.involvementKindIds.size());
    assertSame(expectedOptional, actualOptional);
  }
  @Test
  public void setContactEmailTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setContactEmail("contactEmail");

    // Assert
    assertEquals("contactEmail", json.contactEmail);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setInvolvementKindIdsTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();
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
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setIssuanceKind(SurveyIssuanceKind.GROUP);

    // Assert
    assertEquals(SurveyIssuanceKind.GROUP, json.issuanceKind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setOwnerIdTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setOwnerId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.ownerId);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setStatus(SurveyRunStatus.DRAFT);

    // Assert
    assertEquals(SurveyRunStatus.DRAFT, json.status);
  }
  @Test
  public void setSurveyTemplateIdTest() {
    // Arrange
    ImmutableSurveyRun.Json json = new ImmutableSurveyRun.Json();

    // Act
    json.setSurveyTemplateId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.surveyTemplateId);
  }
}

