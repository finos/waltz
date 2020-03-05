package com.khartec.waltz.model.entity_workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableEntityWorkflowDefinitionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableEntityWorkflowDefinition.Json actualJson = new ImmutableEntityWorkflowDefinition.Json();

    // Assert
    assertNull(actualJson.description);
    assertNull(actualJson.name);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableEntityWorkflowDefinition.Json json = new ImmutableEntityWorkflowDefinition.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableEntityWorkflowDefinition.Json json = new ImmutableEntityWorkflowDefinition.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

