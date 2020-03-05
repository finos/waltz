package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableIdGroupDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableIdGroup.Json()).values.size());
  }


  @Test
  public void setKeyTest() {
    // Arrange
    ImmutableIdGroup.Json json = new ImmutableIdGroup.Json();

    // Act
    json.setKey(1L);

    // Assert
    assertEquals(Long.valueOf(1L), json.key);
  }

  @Test
  public void setValuesTest() {
    // Arrange
    ImmutableIdGroup.Json json = new ImmutableIdGroup.Json();
    ArrayList<Long> resultLongList = new ArrayList<Long>();
    resultLongList.add(1L);

    // Act
    json.setValues(resultLongList);

    // Assert
    assertSame(resultLongList, json.values);
  }
}

