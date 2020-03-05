package com.khartec.waltz.model.performance_metric.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImmutableMetricSampleDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMetricSample.Json actualJson = new ImmutableMetricSample.Json();

    // Assert
    assertNull(actualJson.effectiveDate);
    assertEquals(0L, actualJson.metricId);
    assertNull(actualJson.createdBy);
    assertNull(actualJson.provenance);
    assertNull(actualJson.sampleType);
    assertFalse(actualJson.metricIdIsSet);
    assertNull(actualJson.collectionDate);
  }


  @Test
  public void setCreatedByTest() {
    // Arrange
    ImmutableMetricSample.Json json = new ImmutableMetricSample.Json();

    // Act
    json.setCreatedBy("createdBy");

    // Assert
    assertEquals("createdBy", json.createdBy);
  }

  @Test
  public void setMetricIdTest() {
    // Arrange
    ImmutableMetricSample.Json json = new ImmutableMetricSample.Json();

    // Act
    json.setMetricId(123L);

    // Assert
    assertEquals(123L, json.metricId);
    assertTrue(json.metricIdIsSet);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableMetricSample.Json json = new ImmutableMetricSample.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }

  @Test
  public void setSampleTypeTest() {
    // Arrange
    ImmutableMetricSample.Json json = new ImmutableMetricSample.Json();

    // Act
    json.setSampleType(SampleType.MANUAL);

    // Assert
    assertEquals(SampleType.MANUAL, json.sampleType);
  }
}

