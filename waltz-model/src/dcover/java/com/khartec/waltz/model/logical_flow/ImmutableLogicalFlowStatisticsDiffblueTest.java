package com.khartec.waltz.model.logical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.tally.TallyPack;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableLogicalFlowStatisticsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableLogicalFlowStatistics.Json()).dataTypeCounts.size());
  }
  @Test
  public void setAppCountsTest() {
    // Arrange
    ImmutableLogicalFlowStatistics.Json json = new ImmutableLogicalFlowStatistics.Json();
    ImmutableLogicalFlowMeasures.Json json1 = new ImmutableLogicalFlowMeasures.Json();

    // Act
    json.setAppCounts(json1);

    // Assert
    assertSame(json1, json.appCounts);
  }
  @Test
  public void setDataTypeCountsTest() {
    // Arrange
    ImmutableLogicalFlowStatistics.Json json = new ImmutableLogicalFlowStatistics.Json();
    ArrayList<TallyPack<String>> tallyPackList = new ArrayList<TallyPack<String>>();

    // Act
    json.setDataTypeCounts(tallyPackList);

    // Assert
    assertSame(tallyPackList, json.dataTypeCounts);
  }
  @Test
  public void setFlowCountsTest() {
    // Arrange
    ImmutableLogicalFlowStatistics.Json json = new ImmutableLogicalFlowStatistics.Json();
    ImmutableLogicalFlowMeasures.Json json1 = new ImmutableLogicalFlowMeasures.Json();

    // Act
    json.setFlowCounts(json1);

    // Assert
    assertSame(json1, json.flowCounts);
  }
}

