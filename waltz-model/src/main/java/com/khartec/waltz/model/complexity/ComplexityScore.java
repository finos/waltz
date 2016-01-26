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
     * the raw value this score is based upon
     * @return
     */
    public abstract double rawValue();

    /**
     * score, typically between 0 and 1
     * @return
     */
    public abstract double score();


    /**
     * baseline used to derive this score
     * @return
     */
    public abstract double baseline();


}
