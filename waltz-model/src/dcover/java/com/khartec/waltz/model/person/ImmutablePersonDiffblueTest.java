package com.khartec.waltz.model.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityKindProvider;
import com.khartec.waltz.model.IdProvider;
import java.util.Optional;
import org.junit.Test;

public class ImmutablePersonDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutablePerson.Json actualJson = new ImmutablePerson.Json();

    // Assert
    Optional<String> optional = actualJson.officePhone;
    assertSame(optional, actualJson.mobilePhone);
    assertSame(optional, actualJson.title);
    assertSame(optional, actualJson.departmentName);
    assertSame(optional, actualJson.managerEmployeeId);
    assertSame(optional, actualJson.organisationalUnitId);
    assertSame(optional, actualJson.userPrincipalName);
    assertSame(optional, actualJson.id);
  }
  @Test
  public void setDisplayNameTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setDisplayName("displayName");

    // Assert
    assertEquals("displayName", json.displayName);
  }
  @Test
  public void setEmailTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setEmail("email");

    // Assert
    assertEquals("email", json.email);
  }
  @Test
  public void setEmployeeIdTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setEmployeeId("123");

    // Assert
    assertEquals("123", json.employeeId);
  }
  @Test
  public void setIsRemovedTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setIsRemoved(true);

    // Assert
    assertTrue(json.isRemoved);
    assertTrue(json.isRemovedIsSet);
  }
  @Test
  public void setKindTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.kind);
  }
  @Test
  public void setPersonKindTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setPersonKind(PersonKind.EMPLOYEE);

    // Assert
    assertEquals(PersonKind.EMPLOYEE, json.personKind);
  }
  @Test
  public void setUserIdTest() {
    // Arrange
    ImmutablePerson.Json json = new ImmutablePerson.Json();

    // Act
    json.setUserId("123");

    // Assert
    assertEquals("123", json.userId);
  }
}

