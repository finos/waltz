package com.khartec.waltz.model.involvement;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ImmutableApplicationInvolvementDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableApplicationInvolvement.Json actualJson = new ImmutableApplicationInvolvement.Json();

    // Assert
    assertNull(actualJson.involvement);
    assertNull(actualJson.application);
  }
  @Test
  public void setInvolvementTest() {
    // Arrange
    ImmutableApplicationInvolvement.Json json = new ImmutableApplicationInvolvement.Json();
    ImmutableInvolvement.Json json1 = new ImmutableInvolvement.Json();

    // Act
    json.setInvolvement(json1);

    // Assert
    assertSame(json1, json.involvement);
  }
}

