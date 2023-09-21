package org.finos.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableComplexityDetail.class)
@JsonDeserialize(as = ImmutableMeasurableComplexityDetail.class)
public abstract class MeasurableComplexityDetail {

    public abstract String measurableCategory();
    public abstract String complexityKind();

}
