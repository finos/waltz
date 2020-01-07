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

package com.khartec.waltz.model.entity_statistic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableEntityStatisticValue.class)
@JsonDeserialize(as = ImmutableEntityStatisticValue.class)
public abstract class EntityStatisticValue implements IdProvider, ProvenanceProvider {

    public abstract long statisticId();
    public abstract EntityReference entity();
    public abstract String value();
    public abstract String outcome();
    public abstract StatisticValueState state();
    @Nullable
    public abstract String reason();
    public abstract LocalDateTime createdAt();
    public abstract boolean current();
}
