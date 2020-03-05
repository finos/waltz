package com.khartec.waltz.model.performance_metric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutablePerformanceMetricDefinitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePerformanceMetricDefinition.Json actualJson = new ImmutablePerformanceMetricDefinition.Json();

    // Assert
    assertNull(actualJson.categoryName);
    assertNull(actualJson.name);
    assertNull(actualJson.categoryDescription);
    assertNull(actualJson.description);
  }
  @Test
  public void setCategoryDescriptionTest() {
    // Arrange
    ImmutablePerformanceMetricDefinition.Json json = new ImmutablePerformanceMetricDefinition.Json();

    // Act
    json.setCategoryDescription("categoryDescription");

    // Assert
    assertEquals("categoryDescription", json.categoryDescription);
  }
  @Test
  public void setCategoryNameTest() {
    // Arrange
    ImmutablePerformanceMetricDefinition.Json json = new ImmutablePerformanceMetricDefinition.Json();

    // Act
    json.setCategoryName("categoryName");

    // Assert
    assertEquals("categoryName", json.categoryName);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutablePerformanceMetricDefinition.Json json = new ImmutablePerformanceMetricDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutablePerformanceMetricDefinition.Json json = new ImmutablePerformanceMetricDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

