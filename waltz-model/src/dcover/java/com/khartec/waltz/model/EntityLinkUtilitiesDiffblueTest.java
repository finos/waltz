package com.khartec.waltz.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class EntityLinkUtilitiesDiffblueTest {
  @Test
  public void mkExternalIdLinkTest() {
    // Arrange, Act and Assert
    assertEquals("baseUrlentity/ACTOR/external-id/123",
        EntityLinkUtilities.mkExternalIdLink("baseUrl", EntityKind.ACTOR, "123"));
  }

  @Test
  public void mkIdLinkTest() {
    // Arrange, Act and Assert
    assertEquals("baseUrlentity/ACTOR/id/123", EntityLinkUtilities.mkIdLink("baseUrl", EntityKind.ACTOR, 123L));
  }
}

