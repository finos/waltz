package com.khartec.waltz.model.app_group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ImmutableAppGroupBulkAddRequestDiffblueTest {
    
  @Test
  public void applicationIdsTest2() {
    // Arrange
    ImmutableAppGroupBulkAddRequest.Json json = new ImmutableAppGroupBulkAddRequest.Json();

    // Act
    List<Long> actualApplicationIdsResult = ImmutableAppGroupBulkAddRequest.fromJson(json).applicationIds();

    // Assert
    assertSame(json.unknownIdentifiers, actualApplicationIdsResult);
    assertEquals(0, actualApplicationIdsResult.size());
  }

  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[]," + " unknownIdentifiers=[]}",
        ImmutableAppGroupBulkAddRequest.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppGroupBulkAddRequest.Json actualJson = new ImmutableAppGroupBulkAddRequest.Json();

    // Assert
    List<Long> resultLongList = actualJson.applicationIds;
    List<String> actualStringList = actualJson.unknownIdentifiers;
    assertEquals(0, resultLongList.size());
    assertSame(resultLongList, actualStringList);
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(ImmutableAppGroupBulkAddRequest.fromJson(new ImmutableAppGroupBulkAddRequest.Json()).equals("element"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[]," + " unknownIdentifiers=[]}",
        ImmutableAppGroupBulkAddRequest.fromJson(new ImmutableAppGroupBulkAddRequest.Json()).toString());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(5859943,
        ImmutableAppGroupBulkAddRequest.fromJson(new ImmutableAppGroupBulkAddRequest.Json()).hashCode());
  }

  @Test
  public void setApplicationIdsTest() {
    // Arrange
    ImmutableAppGroupBulkAddRequest.Json json = new ImmutableAppGroupBulkAddRequest.Json();
    ArrayList<Long> resultLongList = new ArrayList<Long>();
    resultLongList.add(1L);

    // Act
    json.setApplicationIds(resultLongList);

    // Assert
    assertSame(resultLongList, json.applicationIds);
  }

  @Test
  public void setUnknownIdentifiersTest() {
    // Arrange
    ImmutableAppGroupBulkAddRequest.Json json = new ImmutableAppGroupBulkAddRequest.Json();
    ArrayList<String> stringList = new ArrayList<String>();
    stringList.add("foo");

    // Act
    json.setUnknownIdentifiers(stringList);

    // Assert
    assertSame(stringList, json.unknownIdentifiers);
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[]," + " unknownIdentifiers=[]}",
        ImmutableAppGroupBulkAddRequest.fromJson(new ImmutableAppGroupBulkAddRequest.Json()).toString());
  }

  @Test
  public void unknownIdentifiersTest2() {
    // Arrange
    ImmutableAppGroupBulkAddRequest.Json json = new ImmutableAppGroupBulkAddRequest.Json();

    // Act
    List<String> actualUnknownIdentifiersResult = ImmutableAppGroupBulkAddRequest.fromJson(json).unknownIdentifiers();

    // Assert
    assertSame(json.unknownIdentifiers, actualUnknownIdentifiersResult);
    assertEquals(0, actualUnknownIdentifiersResult.size());
  }

  @Test
  public void withApplicationIdsTest() {
    // Arrange
    ImmutableAppGroupBulkAddRequest fromJsonResult = ImmutableAppGroupBulkAddRequest
        .fromJson(new ImmutableAppGroupBulkAddRequest.Json());
    ArrayList<Long> resultLongList = new ArrayList<Long>();
    resultLongList.add(1L);

    // Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[1]," + " unknownIdentifiers=[]}",
        fromJsonResult.withApplicationIds(resultLongList).toString());
  }

  @Test
  public void withApplicationIdsTest2() {
    // Arrange
    ImmutableAppGroupBulkAddRequest fromJsonResult = ImmutableAppGroupBulkAddRequest
        .fromJson(new ImmutableAppGroupBulkAddRequest.Json());
    long[] longArray = new long[8];
    Arrays.fill(longArray, 1L);

    // Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[1, 1, 1, 1," + " 1, 1, 1, 1], unknownIdentifiers=[]}",
        fromJsonResult.withApplicationIds(longArray).toString());
  }

  @Test
  public void withUnknownIdentifiersTest() {
    // Arrange
    ImmutableAppGroupBulkAddRequest fromJsonResult = ImmutableAppGroupBulkAddRequest
        .fromJson(new ImmutableAppGroupBulkAddRequest.Json());
    ArrayList<String> stringList = new ArrayList<String>();
    stringList.add("foo");

    // Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[]," + " unknownIdentifiers=[foo]}",
        fromJsonResult.withUnknownIdentifiers(stringList).toString());
  }

  @Test
  public void withUnknownIdentifiersTest2() {
    // Arrange, Act and Assert
    assertEquals("AppGroupBulkAddRequest{applicationIds=[]," + " unknownIdentifiers=[foo, foo, foo]}",
        ImmutableAppGroupBulkAddRequest.fromJson(new ImmutableAppGroupBulkAddRequest.Json())
            .withUnknownIdentifiers("foo", "foo", "foo").toString());
  }
}

