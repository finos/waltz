package com.khartec.waltz.model.software_catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ImmutableSoftwareCatalogDiffblueTest {
    
  @Test
  public void buildTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareCatalog{packages=[], usages=[]," + " versions=[]}",
        ImmutableSoftwareCatalog.builder().build().toString());
  }

  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableSoftwareCatalog.Json actualJson = new ImmutableSoftwareCatalog.Json();

    // Assert
    List<SoftwareUsage> softwareUsageList = actualJson.usages;
    List<SoftwarePackage> actualSoftwarePackageList = actualJson.packages;
    List<SoftwareVersion> actualSoftwareVersionList = actualJson.versions;
    assertEquals(0, softwareUsageList.size());
    assertSame(softwareUsageList, actualSoftwareVersionList);
    assertSame(softwareUsageList, actualSoftwarePackageList);
  }

  @Test
  public void equalsTest() {
    // Arrange, Act and Assert
    assertFalse(ImmutableSoftwareCatalog.fromJson(new ImmutableSoftwareCatalog.Json()).equals("element"));
  }

  @Test
  public void fromJsonTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareCatalog{packages=[], usages=[]," + " versions=[]}",
        ImmutableSoftwareCatalog.fromJson(new ImmutableSoftwareCatalog.Json()).toString());
  }


  @Test
  public void hashCodeTest() {
    // Arrange, Act and Assert
    assertEquals(193378120, ImmutableSoftwareCatalog.fromJson(new ImmutableSoftwareCatalog.Json()).hashCode());
  }

  @Test
  public void packagesTest2() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();

    // Act
    List<SoftwarePackage> actualPackagesResult = ImmutableSoftwareCatalog.fromJson(json).packages();

    // Assert
    assertSame(json.versions, actualPackagesResult);
    assertEquals(0, actualPackagesResult.size());
  }

  @Test
  public void setPackagesTest() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();
    ArrayList<SoftwarePackage> softwarePackageList = new ArrayList<SoftwarePackage>();
    softwarePackageList.add(new ImmutableSoftwarePackage.Json());

    // Act
    json.setPackages(softwarePackageList);

    // Assert
    assertSame(softwarePackageList, json.packages);
  }

  @Test
  public void setUsagesTest() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();
    ArrayList<SoftwareUsage> softwareUsageList = new ArrayList<SoftwareUsage>();
    softwareUsageList.add(new ImmutableSoftwareUsage.Json());

    // Act
    json.setUsages(softwareUsageList);

    // Assert
    assertSame(softwareUsageList, json.usages);
  }

  @Test
  public void setVersionsTest() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();
    ArrayList<SoftwareVersion> softwareVersionList = new ArrayList<SoftwareVersion>();
    softwareVersionList.add(new ImmutableSoftwareVersion.Json());

    // Act
    json.setVersions(softwareVersionList);

    // Assert
    assertSame(softwareVersionList, json.versions);
  }

  @Test
  public void toStringTest() {
    // Arrange, Act and Assert
    assertEquals("SoftwareCatalog{packages=[], usages=[]," + " versions=[]}",
        ImmutableSoftwareCatalog.fromJson(new ImmutableSoftwareCatalog.Json()).toString());
  }

  @Test
  public void usagesTest2() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();

    // Act
    List<SoftwareUsage> actualUsagesResult = ImmutableSoftwareCatalog.fromJson(json).usages();

    // Assert
    assertSame(json.versions, actualUsagesResult);
    assertEquals(0, actualUsagesResult.size());
  }

  @Test
  public void versionsTest2() {
    // Arrange
    ImmutableSoftwareCatalog.Json json = new ImmutableSoftwareCatalog.Json();

    // Act
    List<SoftwareVersion> actualVersionsResult = ImmutableSoftwareCatalog.fromJson(json).versions();

    // Assert
    assertSame(json.versions, actualVersionsResult);
    assertEquals(0, actualVersionsResult.size());
  }
}

