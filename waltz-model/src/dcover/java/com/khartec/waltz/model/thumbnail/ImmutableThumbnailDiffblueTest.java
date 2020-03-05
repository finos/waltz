package com.khartec.waltz.model.thumbnail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.LastUpdatedProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import java.util.Arrays;
import org.junit.Test;

public class ImmutableThumbnailDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableThumbnail.Json actualJson = new ImmutableThumbnail.Json();

    // Assert
    assertNull(actualJson.lastUpdatedAt);
    assertNull(actualJson.mimeType);
    assertNull(actualJson.lastUpdatedBy);
    assertNull(actualJson.blob);
    assertNull(actualJson.provenance);
    assertNull(actualJson.parentEntityReference);
  }





  @Test
  public void setBlobTest() {
    // Arrange
    byte[] byteArray = new byte[24];
    Arrays.fill(byteArray, (byte) 88);
    ImmutableThumbnail.Json json = new ImmutableThumbnail.Json();

    // Act
    json.setBlob(byteArray);

    // Assert
    assertSame(byteArray, json.blob);
  }

  @Test
  public void setLastUpdatedByTest() {
    // Arrange
    ImmutableThumbnail.Json json = new ImmutableThumbnail.Json();

    // Act
    json.setLastUpdatedBy("lastUpdatedBy");

    // Assert
    assertEquals("lastUpdatedBy", json.lastUpdatedBy);
  }

  @Test
  public void setMimeTypeTest() {
    // Arrange
    ImmutableThumbnail.Json json = new ImmutableThumbnail.Json();

    // Act
    json.setMimeType("mimeType");

    // Assert
    assertEquals("mimeType", json.mimeType);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableThumbnail.Json json = new ImmutableThumbnail.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

