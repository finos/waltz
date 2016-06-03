package com.khartec.waltz.model.performance_metric.sample;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@Value.Immutable
@JsonSerialize(as = ImmutableMetricSample.class)
@JsonDeserialize(as = ImmutableMetricSample.class)
public abstract class MetricSample {

    public abstract long metricId();
    public abstract LocalDate collectionDate();
    public abstract LocalDate effectiveDate();
    public abstract SampleType sampleType();
    public abstract String createdBy();
    public abstract String provenance();

}
