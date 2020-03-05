package com.khartec.waltz.model.software_catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.tally.Tally;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableSoftwareSummaryStatisticsDiffblueTest {
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        ImmutableSoftwareSummaryStatistics.builder().build().toString());
  }
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwareSummaryStatistics.Json actualJson = new ImmutableSoftwareSummaryStatistics.Json();

    // Assert
    List<Tally<String>> tallyList = actualJson.nameCounts;
    List<Tally<String>> actualTallyList = actualJson.groupCounts;
    List<Tally<String>> actualTallyList1 = actualJson.vendorCounts;
    assertEquals(0, tallyList.size());
    assertSame(tallyList, actualTallyList1);
    assertSame(tallyList, actualTallyList);
  }
  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(
        ImmutableSoftwareSummaryStatistics.fromJson(new ImmutableSoftwareSummaryStatistics.Json()).equals("element"));
  }
  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        ImmutableSoftwareSummaryStatistics.fromJson(new ImmutableSoftwareSummaryStatistics.Json()).toString());
  }
  @Test
  public void groupCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualGroupCountsResult = ImmutableSoftwareSummaryStatistics.fromJson(json).groupCounts();

    // Assert
    assertSame(json.vendorCounts, actualGroupCountsResult);
    assertEquals(0, actualGroupCountsResult.size());
  }
  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(193378120,
        ImmutableSoftwareSummaryStatistics.fromJson(new ImmutableSoftwareSummaryStatistics.Json()).hashCode());
  }
  @Test
  public void nameCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualNameCountsResult = ImmutableSoftwareSummaryStatistics.fromJson(json).nameCounts();

    // Assert
    assertSame(json.vendorCounts, actualNameCountsResult);
    assertEquals(0, actualNameCountsResult.size());
  }
  @Test
  public void setGroupCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setGroupCounts(tallyList);

    // Assert
    assertSame(tallyList, json.groupCounts);
  }
  @Test
  public void setNameCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setNameCounts(tallyList);

    // Assert
    assertSame(tallyList, json.nameCounts);
  }
  @Test
  public void setVendorCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();
    ArrayList<Tally<String>> tallyList = new ArrayList<Tally<String>>();

    // Act
    json.setVendorCounts(tallyList);

    // Assert
    assertSame(tallyList, json.vendorCounts);
  }
  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        ImmutableSoftwareSummaryStatistics.fromJson(new ImmutableSoftwareSummaryStatistics.Json()).toString());
  }
  @Test
  public void vendorCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics.Json json = new ImmutableSoftwareSummaryStatistics.Json();

    // Act
    List<Tally<String>> actualVendorCountsResult = ImmutableSoftwareSummaryStatistics.fromJson(json).vendorCounts();

    // Assert
    assertSame(json.vendorCounts, actualVendorCountsResult);
    assertEquals(0, actualVendorCountsResult.size());
  }
  @Test
  public void withGroupCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics fromJsonResult = ImmutableSoftwareSummaryStatistics
        .fromJson(new ImmutableSoftwareSummaryStatistics.Json());

    // Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        fromJsonResult.withGroupCounts(new ArrayList<Tally<String>>()).toString());
  }
  @Test
  public void withNameCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics fromJsonResult = ImmutableSoftwareSummaryStatistics
        .fromJson(new ImmutableSoftwareSummaryStatistics.Json());

    // Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        fromJsonResult.withNameCounts(new ArrayList<Tally<String>>()).toString());
  }
  @Test
  public void withVendorCountsTest() {
    // Arrange
    ImmutableSoftwareSummaryStatistics fromJsonResult = ImmutableSoftwareSummaryStatistics
        .fromJson(new ImmutableSoftwareSummaryStatistics.Json());

    // Act and Assert
    assertEquals("SoftwareSummaryStatistics{vendorCounts=[], groupCounts=[]," + " nameCounts=[]}",
        fromJsonResult.withVendorCounts(new ArrayList<Tally<String>>()).toString());
  }
}

