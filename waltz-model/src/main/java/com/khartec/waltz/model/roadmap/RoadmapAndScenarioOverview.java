package com.khartec.waltz.model.roadmap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.scenario.Scenario;
import org.immutables.value.Value;

import java.util.Collection;

@Value.Immutable
@JsonSerialize(as = ImmutableRoadmapAndScenarioOverview.class)
@JsonDeserialize(as = ImmutableRoadmapAndScenarioOverview.class)
public abstract class RoadmapAndScenarioOverview {

    public abstract Roadmap roadmap();
    public abstract Collection<Scenario> scenarios();

}
