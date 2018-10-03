package com.khartec.waltz.model.scenario;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableScenarioAxisItem.class)
@JsonDeserialize(as = ImmutableScenarioAxisItem.class)
public abstract class ScenarioAxisItem implements
        IdProvider {

    public abstract long scenarioId();
    public abstract EntityReference domainItem();
    public abstract int position();
    public abstract AxisOrientation axisOrientation();
}
