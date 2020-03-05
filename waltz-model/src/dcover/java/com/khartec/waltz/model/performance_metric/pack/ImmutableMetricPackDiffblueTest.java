package com.khartec.waltz.model.performance_metric.pack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.checkpoint.Checkpoint;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableMetricPackDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableMetricPack.Json actualJson = new ImmutableMetricPack.Json();

    // Assert
    List<Checkpoint> actualCheckpointList = actualJson.checkpoints;
    List<MetricPackItem> actualMetricPackItemList = actualJson.items;
    List<EntityReference> entityReferenceList = actualJson.relatedReferences;
    assertEquals(0, entityReferenceList.size());
    assertSame(entityReferenceList, actualCheckpointList);
    assertSame(entityReferenceList, actualMetricPackItemList);
  }
  @Test
  public void setCheckpointsTest() {
    // Arrange
    ImmutableMetricPack.Json json = new ImmutableMetricPack.Json();
    ArrayList<Checkpoint> checkpointList = new ArrayList<Checkpoint>();
    checkpointList.add(null);

    // Act
    json.setCheckpoints(checkpointList);

    // Assert
    assertSame(checkpointList, json.checkpoints);
  }
  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableMetricPack.Json json = new ImmutableMetricPack.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }
  @Test
  public void setItemsTest() {
    // Arrange
    ImmutableMetricPack.Json json = new ImmutableMetricPack.Json();
    ArrayList<MetricPackItem> metricPackItemList = new ArrayList<MetricPackItem>();
    metricPackItemList.add(new ImmutableMetricPackItem.Json());

    // Act
    json.setItems(metricPackItemList);

    // Assert
    assertSame(metricPackItemList, json.items);
  }
  @Test
  public void setNameTest() {
    // Arrange
    ImmutableMetricPack.Json json = new ImmutableMetricPack.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }
  @Test
  public void setRelatedReferencesTest() {
    // Arrange
    ImmutableMetricPack.Json json = new ImmutableMetricPack.Json();
    ArrayList<EntityReference> entityReferenceList = new ArrayList<EntityReference>();
    entityReferenceList.add(null);

    // Act
    json.setRelatedReferences(entityReferenceList);

    // Assert
    assertSame(entityReferenceList, json.relatedReferences);
  }
}

