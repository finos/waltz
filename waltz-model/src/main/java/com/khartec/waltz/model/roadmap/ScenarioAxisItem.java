package com.khartec.waltz.model.roadmap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableScenarioAxisItem.class)
@JsonDeserialize(as = ImmutableScenarioAxisItem.class)
public abstract class ScenarioAxisItem implements
        IdProvider {

    public abstract long scenarioId();
    public abstract EntityReference item();
    public abstract int order();
    public abstract AxisKind axisKind();
}
