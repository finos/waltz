package com.khartec.waltz.model.system.job_log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.NameProvider;
import org.junit.Test;

public class ImmutableJobLogDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableJobLog.Json actualJson = new ImmutableJobLog.Json();

    // Assert
    assertNull(actualJson.entityKind);
    assertNull(actualJson.description);
    assertNull(actualJson.start);
    assertNull(actualJson.name);
    assertNull(actualJson.status);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableJobLog.Json json = new ImmutableJobLog.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setEntityKindTest() {
    // Arrange
    ImmutableJobLog.Json json = new ImmutableJobLog.Json();

    // Act
    json.setEntityKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.entityKind);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableJobLog.Json json = new ImmutableJobLog.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableJobLog.Json json = new ImmutableJobLog.Json();

    // Act
    json.setStatus(JobStatus.SUCCESS);

    // Assert
    assertEquals(JobStatus.SUCCESS, json.status);
  }
}

