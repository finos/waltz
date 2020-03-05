package com.khartec.waltz.model.server_information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.tally.Tally;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableServerSummaryStatisticsDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableServerSummaryStatistics.Json actualJson = new ImmutableServerSummaryStatistics.Json();

    // Assert
    List<Tally<String>> tallyList = actualJson.operatingSystemCounts;
    List<Tally<String>> actualTallyList = actualJson.operatingSystemEndOfLifeStatusCounts;
    List<Tally<String>> actualTallyList1 = actualJson.environmentCounts;
    List<Tally<String>> actualTallyList2 = actualJson.hardwareEndOfLifeStatusCounts;
    List<Tally<String>> actualTallyList3 = actualJson.locationCounts;
    assertEquals(0, tallyList.size());
    assertSame(tallyList, actualTallyList3);
    assertSame(tallyList, actualTallyList2);
    assertSame(tallyList, actualTallyList1);
    assertSame(tallyList, actualTallyList);
  }
  @Test
  public void setEnvironmentCountsTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setEnvironmentCounts(tallyList);

    // Assert
    assertSame(tallyList, json.environmentCounts);
  }
  @Test
  public void setHardwareEndOfLifeStatusCountsTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setHardwareEndOfLifeStatusCounts(tallyList);

    // Assert
    assertSame(tallyList, json.hardwareEndOfLifeStatusCounts);
  }
  @Test
  public void setLocationCountsTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setLocationCounts(tallyList);

    // Assert
    assertSame(tallyList, json.locationCounts);
  }
  @Test
  public void setOperatingSystemCountsTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setOperatingSystemCounts(tallyList);

    // Assert
    assertSame(tallyList, json.operatingSystemCounts);
  }
  @Test
  public void setOperatingSystemEndOfLifeStatusCountsTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setOperatingSystemEndOfLifeStatusCounts(tallyList);

    // Assert
    assertSame(tallyList, json.operatingSystemEndOfLifeStatusCounts);
  }
  @Test
  public void setPhysicalCountTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();

    // Act
    json.setPhysicalCount(3L);

    // Assert
    assertTrue(json.physicalCountIsSet);
    assertEquals(3L, json.physicalCount);
  }
  @Test
  public void setTotalCountTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();

    // Act
    json.setTotalCount(3L);

    // Assert
    assertTrue(json.totalCountIsSet);
    assertEquals(3L, json.totalCount);
  }
  @Test
  public void setVirtualCountTest() {
    // Arrange
    ImmutableServerSummaryStatistics.Json json = new ImmutableServerSummaryStatistics.Json();

    // Act
    json.setVirtualCount(3L);

    // Assert
    assertTrue(json.virtualCountIsSet);
    assertEquals(3L, json.virtualCount);
  }
}

