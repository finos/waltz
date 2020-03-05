package com.khartec.waltz.model.app_group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableAppGroupMemberDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppGroupMember.Json actualJson = new ImmutableAppGroupMember.Json();

    // Assert
    assertNull(actualJson.userId);
    assertFalse(actualJson.groupIdIsSet);
    assertEquals(0L, actualJson.groupId);
    assertNull(actualJson.role);
  }
  @Test
  public void setGroupIdTest() {
    // Arrange
    ImmutableAppGroupMember.Json json = new ImmutableAppGroupMember.Json();

    // Act
    json.setGroupId(123L);

    // Assert
    assertTrue(json.groupIdIsSet);
    assertEquals(123L, json.groupId);
  }
  @Test
  public void setRoleTest() {
    // Arrange
    ImmutableAppGroupMember.Json json = new ImmutableAppGroupMember.Json();

    // Act
    json.setRole(AppGroupMemberRole.VIEWER);

    // Assert
    assertEquals(AppGroupMemberRole.VIEWER, json.role);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutableAppGroupMember.Json json = new ImmutableAppGroupMember.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

