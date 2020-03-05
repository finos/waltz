package com.khartec.waltz.model.licence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.ApprovalStatus;
import com.khartec.waltz.model.CreatedUserTimestampProvider;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.ExternalIdProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.LastUpdatedUserTimestampProvider;
import com.khartec.waltz.model.NameProvider;
import com.khartec.waltz.model.ProvenanceProvider;
import com.khartec.waltz.model.UserTimestamp;
import java.util.Optional;
import org.junit.Test;

public class ImmutableLicenceDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableLicence.Json actualJson = new ImmutableLicence.Json();

    // Assert
    Optional<UserTimestamp> optional = actualJson.created;
    assertSame(actualJson.id, optional);
    assertSame(optional, actualJson.id);
    assertSame(optional, actualJson.lastUpdated);
    assertSame(optional, actualJson.externalId);
  }









  @Test
  public void setApprovalStatusTest() {
    // Arrange
    ImmutableLicence.Json json = new ImmutableLicence.Json();

    // Act
    json.setApprovalStatus(ApprovalStatus.PENDING_APPROVAL);

    // Assert
    assertEquals(ApprovalStatus.PENDING_APPROVAL, json.approvalStatus);
  }

  @Test
  public void setDescriptionTest() {
    // Arrange
    ImmutableLicence.Json json = new ImmutableLicence.Json();

    // Act
    json.setDescription("description");

    // Assert
    assertEquals("description", json.description);
  }

  @Test
  public void setNameTest() {
    // Arrange
    ImmutableLicence.Json json = new ImmutableLicence.Json();

    // Act
    json.setName("name");

    // Assert
    assertEquals("name", json.name);
  }

  @Test
  public void setProvenanceTest() {
    // Arrange
    ImmutableLicence.Json json = new ImmutableLicence.Json();

    // Act
    json.setProvenance("provenance");

    // Assert
    assertEquals("provenance", json.provenance);
  }
}

