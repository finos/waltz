package com.khartec.waltz.model.user_agent_info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.khartec.waltz.model.UserNameProvider;
import org.junit.Test;

public class ImmutableUserAgentInfoDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableUserAgentInfo.Json actualJson = new ImmutableUserAgentInfo.Json();

    // Assert
    assertNull(actualJson.userName);
    assertNull(actualJson.ipAddress);
    assertNull(actualJson.userAgent);
    assertNull(actualJson.operatingSystem);
    assertNull(actualJson.resolution);
    assertNull(actualJson.loginTimestamp);
  }



  @Test
  public void setIpAddressTest() {
    // Arrange
    ImmutableUserAgentInfo.Json json = new ImmutableUserAgentInfo.Json();

    // Act
    json.setIpAddress("ipAddress");

    // Assert
    assertEquals("ipAddress", json.ipAddress);
  }

  @Test
  public void setOperatingSystemTest() {
    // Arrange
    ImmutableUserAgentInfo.Json json = new ImmutableUserAgentInfo.Json();

    // Act
    json.setOperatingSystem("operatingSystem");

    // Assert
    assertEquals("operatingSystem", json.operatingSystem);
  }

  @Test
  public void setResolutionTest() {
    // Arrange
    ImmutableUserAgentInfo.Json json = new ImmutableUserAgentInfo.Json();

    // Act
    json.setResolution("resolution");

    // Assert
    assertEquals("resolution", json.resolution);
  }

  @Test
  public void setUserAgentTest() {
    // Arrange
    ImmutableUserAgentInfo.Json json = new ImmutableUserAgentInfo.Json();

    // Act
    json.setUserAgent("userAgent");

    // Assert
    assertEquals("userAgent", json.userAgent);
  }

  @Test
  public void setUserNameTest() {
    // Arrange
    ImmutableUserAgentInfo.Json json = new ImmutableUserAgentInfo.Json();

    // Act
    json.setUserName("username");

    // Assert
    assertEquals("username", json.userName);
  }
}

