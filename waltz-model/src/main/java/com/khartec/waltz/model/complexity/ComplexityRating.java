/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
