package com.khartec.waltz.web.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.Scenario;
import com.khartec.waltz.model.roadmap.ScenarioAxisItem;
import com.khartec.waltz.model.roadmap.ScenarioRatingItem;
import org.immutables.value.Value;

import java.util.Collection;

@Value.Immutable
@JsonSerialize(as = ImmutableFullScenario.class)
@JsonDeserialize(as = ImmutableFullScenario.class)
public abstract class FullScenario {

    public abstract Roadmap roadmap();
    public abstract Scenario scenario();
    public abstract Collection<ScenarioAxisItem> axisDefinitions();
    public abstract Collection<ScenarioRatingItem> ratings();

}
