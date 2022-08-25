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

package org.finos.waltz.model.complexity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.List;


@Value.Immutable
@JsonSerialize(as = ImmutableComplexitySummary.class)
@JsonDeserialize(as = ImmutableComplexitySummary.class)
public abstract class ComplexitySummary {

    public abstract int mappedCount();

    public abstract int missingCount();

    public abstract BigDecimal average();

    public abstract BigDecimal median();

    public abstract BigDecimal variance();

    public abstract BigDecimal standardDeviation();

    public abstract BigDecimal total();

    public abstract List<Complexity> topComplexityScores();

    public abstract ComplexityKind complexityKind();
}
