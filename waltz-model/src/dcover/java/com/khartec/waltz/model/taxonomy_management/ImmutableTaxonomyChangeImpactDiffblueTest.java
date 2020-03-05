package com.khartec.waltz.model.taxonomy_management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Severity;
import java.util.HashSet;
import org.junit.Test;

public class ImmutableTaxonomyChangeImpactDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableTaxonomyChangeImpact.Json()).impactedReferences.size());
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableTaxonomyChangeImpact.Json json = new ImmutableTaxonomyChangeImpact.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setImpactedReferencesTest() {
    // Arrange
    ImmutableTaxonomyChangeImpact.Json json = new ImmutableTaxonomyChangeImpact.Json();
    HashSet<EntityReference> entityReferenceSet = new HashSet<EntityReference>();
    entityReferenceSet.add(null);

    // Act
    json.setImpactedReferences(entityReferenceSet);

    // Assert
    assertSame(entityReferenceSet, json.impactedReferences);
  }
  @Test
  public void setSeverityTest() {
    // Arrange
    ImmutableTaxonomyChangeImpact.Json json = new ImmutableTaxonomyChangeImpact.Json();

    // Act
    json.setSeverity(Severity.INFORMATION);

    // Assert
    assertEquals(Severity.INFORMATION, json.severity);
  }
}

