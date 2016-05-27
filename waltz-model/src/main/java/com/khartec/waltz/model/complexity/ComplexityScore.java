package com.khartec.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityScore.class)
@JsonDeserialize(as = ImmutableComplexityScore.class)
public abstract class ComplexityScore {

    /**
     * Id of the item being scored
     * @return
     */
    public abstract long id();


    /**
     * score, typically between 0 and 1
     * @return
     */
    public abstract double score();


    public abstract ComplexityKind kind();


}
