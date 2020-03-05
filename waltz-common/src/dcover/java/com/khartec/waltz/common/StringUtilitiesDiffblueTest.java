package com.khartec.waltz.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StringUtilitiesDiffblueTest {
  @Test
  public void firstCharTest() {
    // Arrange
    char actualFirstCharResult = StringUtilities.firstChar("", 'A');

    // Act and Assert
    assertEquals('A', actualFirstCharResult);
    assertEquals('s', StringUtilities.firstChar("str", 'A'));
  }

  @Test
  public void ifEmptyTest() {
    // Arrange
    String actualIfEmptyResult = StringUtilities.ifEmpty("", "");

    // Act and Assert
    assertEquals("", actualIfEmptyResult);
    assertEquals("x", StringUtilities.ifEmpty("x", ""));
  }

  @Test
  public void isEmptyTest() {
    // Arrange, Act and Assert
    assertTrue(StringUtilities.isEmpty(""));
  }

  @Test
  public void isNumericLongTest() {
    // Arrange
    boolean actualIsNumericLongResult = StringUtilities.isNumericLong("value");

    // Act and Assert
    assertFalse(actualIsNumericLongResult);
    assertFalse(StringUtilities.isNumericLong(""));
  }

  @Test
  public void joinTest() {
    // Arrange
    ArrayList<Object> objectList = new ArrayList<Object>();
    objectList.add("foo");

    // Act and Assert
    assertEquals("foo", StringUtilities.join(objectList, ""));
  }

  @Test
  public void lengthTest() {
    // Arrange, Act and Assert
    assertEquals(0, StringUtilities.length(""));
  }

  @Test
  public void limitTest() {
    // Arrange, Act and Assert
    assertEquals("", StringUtilities.limit("", 3));
  }

  @Test
  public void lowerTest() {
    // Arrange, Act and Assert
    assertEquals("value", StringUtilities.lower("value"));
  }

  @Test
  public void mkSafeTest() {
    // Arrange, Act and Assert
    assertEquals("", StringUtilities.mkSafe(""));
  }

  @Test
  public void notEmptyTest() {
    // Arrange
    boolean actualNotEmptyResult = StringUtilities.notEmpty("");

    // Act and Assert
    assertFalse(actualNotEmptyResult);
    assertTrue(StringUtilities.notEmpty("x"));
  }

  @Test
  public void parseIntegerTest() {
    // Arrange, Act and Assert
    assertEquals(Integer.valueOf(0), StringUtilities.parseInteger("value", 0));
  }

  @Test
  public void parseLongTest() {
    // Arrange, Act and Assert
    assertEquals(Long.valueOf(1L), StringUtilities.parseLong("value", 1L));
  }

  @Test
  public void tokeniseTest() {
    // Arrange and Act
    List<String> actualTokeniseResult = StringUtilities.tokenise("value", "");

    // Assert
    assertEquals(5, actualTokeniseResult.size());
    assertEquals("v", actualTokeniseResult.get(0));
    assertEquals("a", actualTokeniseResult.get(1));
    assertEquals("l", actualTokeniseResult.get(2));
    assertEquals("u", actualTokeniseResult.get(3));
    assertEquals("e", actualTokeniseResult.get(4));
  }

  @Test
  public void tokeniseTest2() {
    // Arrange, Act and Assert
    assertEquals(0, StringUtilities.tokenise("", "").size());
  }

  @Test
  public void tokeniseTest3() {
    // Arrange, Act and Assert
    assertEquals(0, StringUtilities.tokenise(" ").size());
  }

  @Test
  public void tokeniseTest4() {
    // Arrange and Act
    List<String> actualTokeniseResult = StringUtilities.tokenise("value");

    // Assert
    assertEquals(1, actualTokeniseResult.size());
    assertEquals("value", actualTokeniseResult.get(0));
  }
}

