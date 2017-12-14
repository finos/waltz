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

package com.khartec.waltz.model.rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableRagNames.class)
@JsonDeserialize(as = ImmutableRagNames.class)
public abstract class RagNames {

    @Value.Default
    public RagName R() {
        return ImmutableRagName.builder()
                .rating(RagRating.R)
                .name("Poor")
                .build();
    }


    @Value.Default
    public RagName A() {
        return ImmutableRagName.builder()
                .rating(RagRating.A)
                .name("Adequate")
                .build();
    }


    @Value.Default
    public RagName G() {
        return ImmutableRagName.builder()
                .rating(RagRating.G)
                .name("Good")
                .build();
    }


    @Value.Default
    public RagName Z() {
        return ImmutableRagName.builder()
                .rating(RagRating.Z)
                .name("Unknown")
                .build();
    }


    @Value.Default
    public RagName X() {
        return ImmutableRagName.builder()
                .rating(RagRating.X)
                .name("Not Applicable")
                .build();
    }

}
