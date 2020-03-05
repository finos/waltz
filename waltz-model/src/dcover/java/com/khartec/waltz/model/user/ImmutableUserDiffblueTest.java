package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.UserNameProvider;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableUserDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableUser.Json()).roles.size());
  }



  @Test
  public void setRolesTest() {
    // Arrange
    ImmutableUser.Json json = new ImmutableUser.Json();
    HashSet<String> stringSet = new HashSet<String>();
    stringSet.add("foo");

    // Act
    json.setRoles(stringSet);

    // Assert
    assertSame(stringSet, json.roles);
  }

  @Test
  public void setUserNameTest() {
    // Arrange
    ImmutableUser.Json json = new ImmutableUser.Json();

    // Act
    json.setUserName("username");

    // Assert
    assertEquals("username", json.userName);
  }
}

