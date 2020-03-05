package com.khartec.waltz.model.taxonomy_management;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.CreatedProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import java.util.HashMap;
import org.junit.Test;

public class ImmutableTaxonomyChangeCommandDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange, Act and Assert
    assertNull((new ImmutableTaxonomyChangeCommand.Json()).params);
  }
  @Test
  public void setChangeTypeTest() {
    // Arrange
    ImmutableTaxonomyChangeCommand.Json json = new ImmutableTaxonomyChangeCommand.Json();

    // Act
    json.setChangeType(TaxonomyChangeType.ADD_PEER);

    // Assert
    assertEquals(TaxonomyChangeType.ADD_PEER, json.changeType);
  }
  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutableTaxonomyChangeCommand.Json json = new ImmutableTaxonomyChangeCommand.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableTaxonomyChangeCommand.Json json = new ImmutableTaxonomyChangeCommand.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setParamsTest() {
    // Arrange
    ImmutableTaxonomyChangeCommand.Json json = new ImmutableTaxonomyChangeCommand.Json();
    HashMap<String, String> stringStringMap = new HashMap<String, String>();
    stringStringMap.put("foo", "foo");

    // Act
    json.setParams(stringStringMap);

    // Assert
    assertSame(stringStringMap, json.params);
  }
  @Test
  public void setStatusTest() {
    // Arrange
    ImmutableTaxonomyChangeCommand.Json json = new ImmutableTaxonomyChangeCommand.Json();

    // Act
    json.setStatus(TaxonomyChangeLifecycleStatus.DRAFT);

    // Assert
    assertEquals(TaxonomyChangeLifecycleStatus.DRAFT, json.status);
  }
}

