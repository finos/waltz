package com.khartec.waltz.model.rating;

import static org.junit.Assert.assertEquals;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.PositionProvider;
import org.junit.Test;

public class ImmutableRagNameDiffblueTest {
  @Test
  public void setRatingTest() {
    // Arrange
    ImmutableRagName.Json json = new ImmutableRagName.Json();

    // Act
    json.setRating('A');

    // Assert
    assertEquals(Character.valueOf('A'), json.rating);
  }
}

