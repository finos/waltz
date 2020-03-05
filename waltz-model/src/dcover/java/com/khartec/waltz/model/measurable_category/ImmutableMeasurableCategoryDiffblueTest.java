package com.khartec.waltz.model.measurable_category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.RagNamesProvider;
import com.khartec.waltz.model.rating.RagName;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableMeasurableCategoryDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMeasurableCategory.Json actualJson = new ImmutableMeasurableCategory.Json();

    // Assert
    assertSame(actualJson.externalId, actualJson.id);
    assertEquals(0, actualJson.ragNames.size());
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEditableTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setEditable(true);

    // Assert
    assertTrue(json.editable);
    assertTrue(json.editableIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setRagNamesTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();
    ArrayList<RagName> ragNameList = new ArrayList<RagName>();
    ragNameList.add(null);

    // Act
    json.setRagNames(ragNameList);

    // Assert
    assertSame(ragNameList, json.ragNames);
  }
  @Test
  public void setRatingSchemeIdTest() {
    // Arrange
    ImmutableMeasurableCategory.Json json = new ImmutableMeasurableCategory.Json();

    // Act
    json.setRatingSchemeId(123L);

    // Assert
    assertEquals(123L, json.ratingSchemeId);
    assertTrue(json.ratingSchemeIdIsSet);
  }
}

