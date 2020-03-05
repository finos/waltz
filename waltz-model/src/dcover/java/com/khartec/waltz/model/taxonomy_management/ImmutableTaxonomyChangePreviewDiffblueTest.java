package com.khartec.waltz.model.taxonomy_management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableTaxonomyChangePreviewDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertEquals(0, (new ImmutableTaxonomyChangePreview.Json()).impacts.size());
  }


  @Test
  public void setCommandTest() {
    // Arrange
    ImmutableTaxonomyChangePreview.Json json = new ImmutableTaxonomyChangePreview.Json();
    ImmutableTaxonomyChangeCommand.Json json1 = new ImmutableTaxonomyChangeCommand.Json();

    // Act
    json.setCommand(json1);

    // Assert
    assertSame(json1, json.command);
  }

  @Test
  public void setImpactsTest() {
    // Arrange
    ImmutableTaxonomyChangePreview.Json json = new ImmutableTaxonomyChangePreview.Json();
    ArrayList<TaxonomyChangeImpact> taxonomyChangeImpactList = new ArrayList<TaxonomyChangeImpact>();
    taxonomyChangeImpactList.add(new ImmutableTaxonomyChangeImpact.Json());

    // Act
    json.setImpacts(taxonomyChangeImpactList);

    // Assert
    assertSame(taxonomyChangeImpactList, json.impacts);
  }
}

