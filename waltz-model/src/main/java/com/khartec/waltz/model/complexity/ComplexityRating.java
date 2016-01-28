package com.khartec.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityRating.class)
@JsonDeserialize(as = ImmutableComplexityRating.class)
public abstract class ComplexityRating {

    public abstract long id();
    public abstract Optional<ComplexityScore> serverComplexity();
    public abstract Optional<ComplexityScore> connectionComplexity();
    public abstract Optional<ComplexityScore> capabilityComplexity();

    @Value.Derived
    public double overallScore() {

        int availableScores = 0;
        double runningTotal = 0;

        if (serverComplexity().isPresent()) {
            availableScores++;
            runningTotal += serverComplexity().get().score();
        }

        if (connectionComplexity().isPresent()) {
            availableScores++;
            runningTotal += connectionComplexity().get().score();
        }

        if (capabilityComplexity().isPresent()) {
            availableScores++;
            runningTotal += capabilityComplexity().get().score();
        }

        return availableScores > 0 ? runningTotal / availableScores : 0;
    }

}
