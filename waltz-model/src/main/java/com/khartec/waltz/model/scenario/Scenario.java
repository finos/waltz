package com.khartec.waltz.model.scenario;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableScenario.class)
@JsonDeserialize(as = ImmutableScenario.class)
public abstract class Scenario implements
        IdProvider,
        NameProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        EntityLifecycleStatusProvider {

    public abstract long roadmapId();
    public abstract LocalDate effectiveDate();
    public abstract ScenarioType scenarioType();
    public abstract ReleaseLifecycleStatus releaseStatus();

}
