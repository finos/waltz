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

package org.finos.waltz.model.allocation;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRatingPercentage.class)
@JsonDeserialize(as = ImmutableMeasurableRatingPercentage.class)
public abstract class MeasurableRatingPercentage {

    public abstract int percentage();
    public abstract long measurableRatingId();


    public static MeasurableRatingPercentage mkMeasurableRatingPercentage(long measurableRatingId, int percentage) {
        return ImmutableMeasurableRatingPercentage
                .builder()
                .percentage(percentage)
                .measurableRatingId(measurableRatingId)
                .build();
    }

}
