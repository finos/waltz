package com.khartec.waltz.model.logical_flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.CreatedUserTimestampProvider;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityLifecycleStatusProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastAttestedProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.UserTimestamp;
import java.util.Optional;
import org.junit.Test;

public class ImmutableLogicalFlowDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLogicalFlow.Json actualJson = new ImmutableLogicalFlow.Json();

    // Assert
    Optional<UserTimestamp> optional = actualJson.created;
    assertSame(actualJson.lastAttestedBy, optional);
    assertSame(optional, actualJson.lastAttestedBy);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.lastAttestedAt);
  }
  @Test
  public void setEntityLifecycleStatusTest() {
    // Arrange
    ImmutableLogicalFlow.Json json = new ImmutableLogicalFlow.Json();

    // Act
    json.setEntityLifecycleStatus(EntityLifecycleStatus.ACTIVE);

    // Assert
    assertEquals(EntityLifecycleStatus.ACTIVE, json.entityLifecycleStatus);
  }
  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableLogicalFlow.Json json = new ImmutableLogicalFlow.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }
  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableLogicalFlow.Json json = new ImmutableLogicalFlow.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

