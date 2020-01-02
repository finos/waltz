/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableComplexityRating.class)
@JsonDeserialize(as = ImmutableComplexityRating.class)
public abstract class ComplexityRating {

    public abstract long id();
    public abstract Optional<ComplexityScore> serverComplexity();
    public abstract Optional<ComplexityScore> connectionComplexity();
    public abstract Optional<ComplexityScore> measurableComplexity();

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

        if (measurableComplexity().isPresent()) {
            availableScores++;
            runningTotal += measurableComplexity().get().score();
        }

        return availableScores > 0 ? runningTotal / availableScores : 0;
    }

}
