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

package org.finos.waltz.model.measurable_rating;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.finos.waltz.model.DescriptionProvider;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdProvider;
import org.finos.waltz.model.LastUpdatedProvider;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.ProvenanceProvider;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutableMeasurableRating.class)
@JsonDeserialize(as = ImmutableMeasurableRating.class)
public abstract class MeasurableRating implements
        IdProvider,
        DescriptionProvider,
        LastUpdatedProvider,
        ProvenanceProvider {

    public abstract EntityReference entityReference();

    public abstract long measurableId();

    public abstract char rating();

    @Value.Default
    public boolean isReadOnly() {
        return false;
    }

    @Value.Default
    public boolean isPrimary() {
        return false;
    }

    // The aim is to move to ratingId long term and remove the 'code' column
    @Nullable
    public abstract Long ratingId();
}
