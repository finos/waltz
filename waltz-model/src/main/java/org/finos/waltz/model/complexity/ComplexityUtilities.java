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

import org.finos.waltz.model.tally.Tally;

import java.util.function.Function;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;

/**
 * To be removed in 1.32
 */
@Deprecated
public class ComplexityUtilities {

    public static ComplexityScore tallyToComplexityScore(ComplexityType kind,
                                                         Tally<Long> tally,
                                                         double baseline) {
        return tallyToComplexityScore(kind, tally, baseline, Function.identity());
    }


    public static ComplexityScore tallyToComplexityScore(ComplexityType kind,
                                                         Tally<Long> tally,
                                                         double baseline,
                                                         Function<Double, Double> valueTransformer) {
        checkNotNull(tally, "Cannot create a complexity score from a null tally");
        checkTrue(baseline >= 0, "Cannot create a complexity score with a negative baseline value");
        checkNotNull(valueTransformer, "valueTransformer cannot be null");

        double transformedBaseline = valueTransformer.apply(baseline);
        double transformedTally = valueTransformer.apply(tally.count());

        double score = baseline == 0
                ? 0
                : transformedTally / transformedBaseline;

        return ImmutableComplexityScore.builder()
                .id(tally.id())
                .score(score)
                .kind(kind)
                .build();
    }

}
