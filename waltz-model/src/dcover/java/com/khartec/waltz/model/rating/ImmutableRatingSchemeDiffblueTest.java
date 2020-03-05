package com.khartec.waltz.model.rating;

import static org.junit.Assert.assertEquals;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableRatingSchemeDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableRatingScheme.Json()).ratings.size());
  }





  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableRatingScheme.Json json = new ImmutableRatingScheme.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableRatingScheme.Json json = new ImmutableRatingScheme.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
}

