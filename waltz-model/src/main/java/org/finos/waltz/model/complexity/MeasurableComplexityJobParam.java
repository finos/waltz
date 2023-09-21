package org.finos.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableComplexityJobParam.class)
@JsonDeserialize(as = ImmutableMeasurableComplexityJobParam.class)
public abstract class MeasurableComplexityJobParam {

    public abstract Set<MeasurableComplexityDetail> measurableComplexities();

}
