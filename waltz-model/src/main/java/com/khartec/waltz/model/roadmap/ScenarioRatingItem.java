package com.khartec.waltz.model.roadmap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableScenarioRatingItem.class)
@JsonDeserialize(as = ImmutableScenarioRatingItem.class)
public abstract class ScenarioRatingItem {

    public abstract long scenarioId();
    public abstract char rating();
    public abstract EntityReference item();
    public abstract EntityReference row();
    public abstract EntityReference column();

}
