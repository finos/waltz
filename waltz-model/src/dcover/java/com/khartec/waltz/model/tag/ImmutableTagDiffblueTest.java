package com.khartec.waltz.model.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableTagDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableTag.Json()).tagUsages.size());
  }




  @Test
  public void setNameTest() {
    // Arrange
    ImmutableTag.Json json = new ImmutableTag.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setTagUsagesTest() {
    // Arrange
    ImmutableTag.Json json = new ImmutableTag.Json();
    HashSet<TagUsage> tagUsageSet = new HashSet<TagUsage>();
    tagUsageSet.add(new ImmutableTagUsage.Json());

    // Act
    json.setTagUsages(tagUsageSet);

    // Assert
    assertSame(tagUsageSet, json.tagUsages);
  }

  @Test
  public void setTargetKindTest() {
    // Arrange
    ImmutableTag.Json json = new ImmutableTag.Json();

    // Act
    json.setTargetKind(EntityKind.ACTOR);

    // Assert
    assertEquals(EntityKind.ACTOR, json.targetKind);
  }
}

