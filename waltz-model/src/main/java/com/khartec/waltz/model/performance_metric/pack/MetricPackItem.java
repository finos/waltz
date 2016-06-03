package com.khartec.waltz.model.performance_metric.pack;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.checkpoint.CheckpointGoal;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableMetricPackItem.class)
@JsonDeserialize(as = ImmutableMetricPackItem.class)
public abstract class MetricPackItem implements
        IdProvider {

    public abstract long definitionId();
    public abstract String sectionName();
    public abstract double baseLine();
    public abstract List<CheckpointGoal> goals();

}
