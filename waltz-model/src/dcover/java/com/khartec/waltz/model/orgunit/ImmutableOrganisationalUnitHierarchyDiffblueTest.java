package com.khartec.waltz.model.orgunit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableOrganisationalUnitHierarchyDiffblueTest {
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableOrganisationalUnitHierarchy.Json actualJson = new ImmutableOrganisationalUnitHierarchy.Json();

    // Assert
    List<OrganisationalUnit> organisationalUnitList = actualJson.children;
    List<OrganisationalUnit> actualOrganisationalUnitList = actualJson.parents;
    assertEquals(0, organisationalUnitList.size());
    assertSame(organisationalUnitList, actualOrganisationalUnitList);
  }
  @Test
  public void setChildrenTest() {
    // Arrange
    ImmutableOrganisationalUnitHierarchy.Json json = new ImmutableOrganisationalUnitHierarchy.Json();
    ArrayList<OrganisationalUnit> organisationalUnitList = new ArrayList<OrganisationalUnit>();
    organisationalUnitList.add(new ImmutableOrganisationalUnit.Json());

    // Act
    json.setChildren(organisationalUnitList);

    // Assert
    assertSame(organisationalUnitList, json.children);
  }
  @Test
  public void setParentsTest() {
    // Arrange
    ImmutableOrganisationalUnitHierarchy.Json json = new ImmutableOrganisationalUnitHierarchy.Json();
    ArrayList<OrganisationalUnit> organisationalUnitList = new ArrayList<OrganisationalUnit>();
    organisationalUnitList.add(new ImmutableOrganisationalUnit.Json());

    // Act
    json.setParents(organisationalUnitList);

    // Assert
    assertSame(organisationalUnitList, json.parents);
  }
  @Test
  public void setUnitTest() {
    // Arrange
    ImmutableOrganisationalUnitHierarchy.Json json = new ImmutableOrganisationalUnitHierarchy.Json();
    ImmutableOrganisationalUnit.Json json1 = new ImmutableOrganisationalUnit.Json();

    // Act
    json.setUnit(json1);

    // Assert
    assertSame(json1, json.unit);
  }
}

