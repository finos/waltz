package com.khartec.waltz.model.roadmap;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import com.khartec.waltz.model.scenario.Scenario;
import java.util.ArrayList;
import org.junit.Test;

public class ImmutableRoadmapAndScenarioOverviewDiffblueTest {
    
  @Test
  public void constructorTest() {
    // Arrange and Act
    ImmutableRoadmapAndScenarioOverview.Json actualJson = new ImmutableRoadmapAndScenarioOverview.Json();

    // Assert
    assertNull(actualJson.roadmap);
    assertNull(actualJson.scenarios);
  }


  @Test
  public void setRoadmapTest() {
    // Arrange
    ImmutableRoadmapAndScenarioOverview.Json json = new ImmutableRoadmapAndScenarioOverview.Json();
    ImmutableRoadmap.Json json1 = new ImmutableRoadmap.Json();

    // Act
    json.setRoadmap(json1);

    // Assert
    assertSame(json1, json.roadmap);
  }

  @Test
  public void setScenariosTest() {
    // Arrange
    ImmutableRoadmapAndScenarioOverview.Json json = new ImmutableRoadmapAndScenarioOverview.Json();
    ArrayList<Scenario> scenarioList = new ArrayList<Scenario>();
    scenarioList.add(null);

    // Act
    json.setScenarios(scenarioList);

    // Assert
    assertSame(scenarioList, json.scenarios);
  }
}

