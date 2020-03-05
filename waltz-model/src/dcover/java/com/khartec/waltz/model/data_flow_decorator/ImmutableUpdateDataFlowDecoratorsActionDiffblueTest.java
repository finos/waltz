package com.khartec.waltz.model.data_flow_decorator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.EntityReference;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class ImmutableUpdateDataFlowDecoratorsActionDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUpdateDataFlowDecoratorsAction.Json actualJson = new ImmutableUpdateDataFlowDecoratorsAction.Json();

    // Assert
    Set<EntityReference> entityReferenceSet = actualJson.addedDecorators;
    Set<EntityReference> actualEntityReferenceSet = actualJson.removedDecorators;
    assertEquals(0, entityReferenceSet.size());
    assertSame(entityReferenceSet, actualEntityReferenceSet);
  }
  @Test
  public void setAddedDecoratorsTest() {
    // Arrange
    ImmutableUpdateDataFlowDecoratorsAction.Json json = new ImmutableUpdateDataFlowDecoratorsAction.Json();
    HashSet<EntityReference> entityReferenceSet = new HashSet<EntityReference>();
    entityReferenceSet.add(null);

    // Act
    json.setAddedDecorators(entityReferenceSet);

    // Assert
    assertSame(entityReferenceSet, json.addedDecorators);
  }
  @Test
  public void setFlowIdTest() {
    // Arrange
    ImmutableUpdateDataFlowDecoratorsAction.Json json = new ImmutableUpdateDataFlowDecoratorsAction.Json();

    // Act
    json.setFlowId(123L);

    // Assert
    assertEquals(Long.valueOf(123L), json.flowId);
  }
  @Test
  public void setRemovedDecoratorsTest() {
    // Arrange
    ImmutableUpdateDataFlowDecoratorsAction.Json json = new ImmutableUpdateDataFlowDecoratorsAction.Json();
    HashSet<EntityReference> entityReferenceSet = new HashSet<EntityReference>();
    entityReferenceSet.add(null);

    // Act
    json.setRemovedDecorators(entityReferenceSet);

    // Assert
    assertSame(entityReferenceSet, json.removedDecorators);
  }
}

