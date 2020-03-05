package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableEntityReferenceKeyedGroupDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableEntityReferenceKeyedGroup.Json()).values.size());
  }
  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableEntityReferenceKeyedGroup.Json json = new ImmutableEntityReferenceKeyedGroup.Json();
    ImmutableEntityReference.Json json1 = new ImmutableEntityReference.Json();

    // Act
    json.setKey(json1);

    // Assert
    assertSame(json1, json.key);
  }
  @Test
  public void setValuesTest() {
    // Arrange
    ImmutableEntityReferenceKeyedGroup.Json json = new ImmutableEntityReferenceKeyedGroup.Json();
    ArrayList<EntityReference> entityReferenceList = new ArrayList<EntityReference>();
    entityReferenceList.add(new ImmutableEntityReference.Json());

    // Act
    json.setValues(entityReferenceList);

    // Assert
    assertSame(entityReferenceList, json.values);
  }
}

