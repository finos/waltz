package com.khartec.waltz.model.performance_metric;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.DescriptionProvider;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.NameProvider;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutablePerformanceMetricDefinition.class)
@JsonDeserialize(as = ImmutablePerformanceMetricDefinition.class)
public abstract class PerformanceMetricDefinition implements
        NameProvider,
        DescriptionProvider,
        IdProvider {

    public abstract String categoryName();
    public abstract String categoryDescription();

}
