package com.khartec.waltz.model.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Set;
import org.junit.Test;

public class SystemRoleDiffblueTest {
  @Test
  public void allNamesTest() {
    // Arrange and Act
    Set<String> actualAllNamesResult = SystemRole.allNames();

    // Assert
    assertEquals(20, actualAllNamesResult.size());
    assertTrue(actualAllNamesResult.contains("ANONYMOUS"));
    assertTrue(actualAllNamesResult.contains("RATING_EDITOR"));
    assertTrue(actualAllNamesResult.contains("ORG_UNIT_EDITOR"));
    assertTrue(actualAllNamesResult.contains("CAPABILITY_EDITOR"));
    assertTrue(actualAllNamesResult.contains("SCENARIO_ADMIN"));
    assertTrue(actualAllNamesResult.contains("USER_ADMIN"));
    assertTrue(actualAllNamesResult.contains("BOOKMARK_EDITOR"));
    assertTrue(actualAllNamesResult.contains("ATTESTATION_ADMIN"));
    assertTrue(actualAllNamesResult.contains("TAXONOMY_EDITOR"));
    assertTrue(actualAllNamesResult.contains("SURVEY_TEMPLATE_ADMIN"));
    assertTrue(actualAllNamesResult.contains("BETA_TESTER"));
  }
}

