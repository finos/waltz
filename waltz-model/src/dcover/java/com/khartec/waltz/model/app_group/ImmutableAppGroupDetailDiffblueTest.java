package com.khartec.waltz.model.app_group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.EntityReference;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableAppGroupDetailDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableAppGroupDetail.Json actualJson = new ImmutableAppGroupDetail.Json();

    // Assert
    List<AppGroupMember> actualAppGroupMemberList = actualJson.members;
    List<EntityReference> actualEntityReferenceList = actualJson.applications;
    List<EntityReference> entityReferenceList = actualJson.organisationalUnits;
    assertEquals(0, entityReferenceList.size());
    assertSame(entityReferenceList, actualEntityReferenceList);
    assertSame(entityReferenceList, actualAppGroupMemberList);
  }
  @Test
  public void setAppGroupTest() {
    // Arrange
    ImmutableAppGroupDetail.Json json = new ImmutableAppGroupDetail.Json();
    ImmutableAppGroup.Json json1 = new ImmutableAppGroup.Json();

    // Act
    json.setAppGroup(json1);

    // Assert
    assertSame(json1, json.appGroup);
  }
  @Test
  public void setApplicationsTest() {
    // Arrange
    ImmutableAppGroupDetail.Json json = new ImmutableAppGroupDetail.Json();
    ArrayList<EntityReference> entityReferenceList = new ArrayList<EntityReference>();
    entityReferenceList.add(null);

    // Act
    json.setApplications(entityReferenceList);

    // Assert
    assertSame(entityReferenceList, json.applications);
  }
  @Test
  public void setMembersTest() {
    // Arrange
    ImmutableAppGroupDetail.Json json = new ImmutableAppGroupDetail.Json();
    ArrayList<AppGroupMember> appGroupMemberList = new ArrayList<AppGroupMember>();
    appGroupMemberList.add(new ImmutableAppGroupMember.Json());

    // Act
    json.setMembers(appGroupMemberList);

    // Assert
    assertSame(appGroupMemberList, json.members);
  }
  @Test
  public void setOrganisationalUnitsTest() {
    // Arrange
    ImmutableAppGroupDetail.Json json = new ImmutableAppGroupDetail.Json();
    ArrayList<EntityReference> entityReferenceList = new ArrayList<EntityReference>();
    entityReferenceList.add(null);

    // Act
    json.setOrganisationalUnits(entityReferenceList);

    // Assert
    assertSame(entityReferenceList, json.organisationalUnits);
  }
}

