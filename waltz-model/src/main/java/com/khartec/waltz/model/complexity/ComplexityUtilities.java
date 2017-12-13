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

import com.khartec.waltz.model.tally.Tally;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;


public class ComplexityUtilities {

    public static ComplexityScore tallyToComplexityScore(ComplexityKind kind,
                                                         Tally<Long> tally,
                                                         double baseline) {
        return tallyToComplexityScore(kind, tally, baseline, Function.identity());
    }


    public static ComplexityScore tallyToComplexityScore(ComplexityKind kind,
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
