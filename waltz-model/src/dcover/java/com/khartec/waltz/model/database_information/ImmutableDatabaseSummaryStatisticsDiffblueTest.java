package com.khartec.waltz.model.database_information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.tally.Tally;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableDatabaseSummaryStatisticsDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        ImmutableDatabaseSummaryStatistics.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDatabaseSummaryStatistics.Json actualJson = new ImmutableDatabaseSummaryStatistics.Json();

    // Assert
    List<Tally<String>> tallyList = actualJson.environmentCounts;
    List<Tally<String>> actualTallyList = actualJson.vendorCounts;
    List<Tally<String>> actualTallyList1 = actualJson.endOfLifeStatusCounts;
    assertEquals(0, tallyList.size());
    assertSame(tallyList, actualTallyList1);
    assertSame(tallyList, actualTallyList);
  }

  @Test
  public void endOfLifeStatusCountsTest2() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualEndOfLifeStatusCountsResult = ImmutableDatabaseSummaryStatistics.fromJson(json)
        .endOfLifeStatusCounts();

    // Assert
    assertSame(json.endOfLifeStatusCounts, actualEndOfLifeStatusCountsResult);
    assertEquals(0, actualEndOfLifeStatusCountsResult.size());
  }

  @Test
  public void environmentCountsTest2() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualEnvironmentCountsResult = ImmutableDatabaseSummaryStatistics.fromJson(json)
        .environmentCounts();

    // Assert
    assertSame(json.endOfLifeStatusCounts, actualEnvironmentCountsResult);
    assertEquals(0, actualEnvironmentCountsResult.size());
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(
        ImmutableDatabaseSummaryStatistics.fromJson(new ImmutableDatabaseSummaryStatistics.Json()).equals("element"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        ImmutableDatabaseSummaryStatistics.fromJson(new ImmutableDatabaseSummaryStatistics.Json()).toString());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(193378120,
        ImmutableDatabaseSummaryStatistics.fromJson(new ImmutableDatabaseSummaryStatistics.Json()).hashCode());
  }

  @Test
  public void setEndOfLifeStatusCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setEndOfLifeStatusCounts(tallyList);

    // Assert
    assertSame(tallyList, json.endOfLifeStatusCounts);
  }

  @Test
  public void setEnvironmentCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setEnvironmentCounts(tallyList);

    // Assert
    assertSame(tallyList, json.environmentCounts);
  }

  @Test
  public void setVendorCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setVendorCounts(tallyList);

    // Assert
    assertSame(tallyList, json.vendorCounts);
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        ImmutableDatabaseSummaryStatistics.fromJson(new ImmutableDatabaseSummaryStatistics.Json()).toString());
  }

  @Test
  public void vendorCountsTest2() {
    // Arrange
    ImmutableDatabaseSummaryStatistics.Json json = new ImmutableDatabaseSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualVendorCountsResult = ImmutableDatabaseSummaryStatistics.fromJson(json).vendorCounts();

    // Assert
    assertSame(json.endOfLifeStatusCounts, actualVendorCountsResult);
    assertEquals(0, actualVendorCountsResult.size());
  }

  @Test
  public void withEndOfLifeStatusCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics fromJsonResult = ImmutableDatabaseSummaryStatistics
        .fromJson(new ImmutableDatabaseSummaryStatistics.Json());

    // Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        fromJsonResult.withEndOfLifeStatusCounts(new ArrayList<Tally<String>>()).toString());
  }

  @Test
  public void withEnvironmentCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics fromJsonResult = ImmutableDatabaseSummaryStatistics
        .fromJson(new ImmutableDatabaseSummaryStatistics.Json());

    // Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        fromJsonResult.withEnvironmentCounts(new ArrayList<Tally<String>>()).toString());
  }

  @Test
  public void withVendorCountsTest() {
    // Arrange
    ImmutableDatabaseSummaryStatistics fromJsonResult = ImmutableDatabaseSummaryStatistics
        .fromJson(new ImmutableDatabaseSummaryStatistics.Json());

    // Act and Assert
    assertEquals("DatabaseSummaryStatistics{environmentCounts=[]," + " vendorCounts=[], endOfLifeStatusCounts=[]}",
        fromJsonResult.withVendorCounts(new ArrayList<Tally<String>>()).toString());
  }
}

