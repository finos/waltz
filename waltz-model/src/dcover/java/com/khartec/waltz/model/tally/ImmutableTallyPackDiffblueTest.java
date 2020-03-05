package com.khartec.waltz.model.tally;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableTallyPackDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableTallyPack.Json<Object>()).tallies.size());
  }
  @Test
  public void setTalliesTest() {
    // Arrange
    ImmutableTallyPack.Json<Object> json = new ImmutableTallyPack.Json<Object>();
    ArrayList<Tally<Object>> tallyList = new ArrayList<Tally<Object>>();
    tallyList.add(new ImmutableOrderedTally.Json<Object>());

    // Act
    json.setTallies(tallyList);

    // Assert
    assertSame(tallyList, json.tallies);
  }
}

