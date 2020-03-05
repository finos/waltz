package com.khartec.waltz.model.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableSettingDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSetting.Json actualJson = new ImmutableSetting.Json();

    // Assert
    assertFalse(actualJson.restricted);
    assertNull(actualJson.name);
    assertFalse(actualJson.restrictedIsSet);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableSetting.Json json = new ImmutableSetting.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setRestrictedTest() {
    // Arrange
    ImmutableSetting.Json json = new ImmutableSetting.Json();

    // Act
    json.setRestricted(true);

    // Assert
    assertTrue(json.restricted);
    assertTrue(json.restrictedIsSet);
  }
}

