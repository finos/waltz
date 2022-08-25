package org.finos.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityTotal.class)
public abstract class ComplexityTotal {

    public abstract ComplexityKind complexityKind();
    public abstract BigDecimal total();
    public abstract BigDecimal average();

    public abstract BigDecimal standardDeviation();

}
