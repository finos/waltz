package com.khartec.waltz.model.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import java.util.Date;
import org.junit.Test;

public class ImmutableDateFieldChangeDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange and Act
    ImmutableDateFieldChange actualBuildResult = ImmutableDateFieldChange.builder().build();

    // Assert
    assertEquals("DateFieldChange{newVal=null, oldVal=null," + " description=null}", actualBuildResult.toString());
    assertNull(actualBuildResult.description());
  }

  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableDateFieldChange.Json actualJson = new ImmutableDateFieldChange.Json();

    // Assert
    assertNull(actualJson.newVal);
    assertNull(actualJson.oldVal);
    assertNull(actualJson.description);
  }

  @Test
  public void descriptionTest2() {
    // Arrange, Act and Assert
    assertNull(ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).description());
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).equals("another"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange and Act
    ImmutableDateFieldChange actualFromJsonResult = ImmutableDateFieldChange
        .fromJson(new ImmutableDateFieldChange.Json());

    // Assert
    assertEquals("DateFieldChange{newVal=null, oldVal=null," + " description=null}", actualFromJsonResult.toString());
    assertNull(actualFromJsonResult.description());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(193376997, ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).hashCode());
  }

  @Test
  public void newValTest2() {
    // Arrange, Act and Assert
    assertNull(ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).newVal());
  }

  @Test
  public void oldValTest2() {
    // Arrange, Act and Assert
    assertNull(ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).oldVal());
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableDateFieldChange.Json json = new ImmutableDateFieldChange.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setNewValTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableDateFieldChange.Json json = new ImmutableDateFieldChange.Json();

    // Act
    json.setNewVal(date);

    // Assert
    assertSame(date, json.newVal);
  }

  @Test
  public void setOldValTest() {
    // Arrange
    Date date = new Date(1L);
    ImmutableDateFieldChange.Json json = new ImmutableDateFieldChange.Json();

    // Act
    json.setOldVal(date);

    // Assert
    assertSame(date, json.oldVal);
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("DateFieldChange{newVal=null, oldVal=null," + " description=null}",
        ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json()).toString());
  }

  @Test
  public void withDescriptionTest() {
    // Arrange and Act
    ImmutableDateFieldChange actualWithDescriptionResult = ImmutableDateFieldChange
        .fromJson(new ImmutableDateFieldChange.Json()).withDescription("value");

    // Assert
    assertEquals("DateFieldChange{newVal=null, oldVal=null," + " description=value}",
        actualWithDescriptionResult.toString());
    assertEquals("value", actualWithDescriptionResult.description());
  }

  @Test
  public void withNewValTest() {
    // Arrange
    ImmutableDateFieldChange fromJsonResult = ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json());

    // Act
    ImmutableDateFieldChange actualWithNewValResult = fromJsonResult.withNewVal(new Date(1L));

    // Assert
    assertEquals("DateFieldChange{newVal=Thu Jan 01 01:00:00 GMT 1970," + " oldVal=null, description=null}",
        actualWithNewValResult.toString());
    assertNull(actualWithNewValResult.description());
  }

  @Test
  public void withOldValTest() {
    // Arrange
    ImmutableDateFieldChange fromJsonResult = ImmutableDateFieldChange.fromJson(new ImmutableDateFieldChange.Json());

    // Act
    ImmutableDateFieldChange actualWithOldValResult = fromJsonResult.withOldVal(new Date(1L));

    // Assert
    assertEquals("DateFieldChange{newVal=null, oldVal=Thu Jan 01" + " 01:00:00 GMT 1970, description=null}",
        actualWithOldValResult.toString());
    assertNull(actualWithOldValResult.description());
  }
}

