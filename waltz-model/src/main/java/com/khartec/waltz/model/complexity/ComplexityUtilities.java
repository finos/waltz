package com.khartec.waltz.model.complexity;

import com.khartec.waltz.model.tally.LongTally;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;


public class ComplexityUtilities {

    public static ComplexityScore tallyToComplexityScore(LongTally tally, double baseline) {
        return tallyToComplexityScore(tally, baseline, Function.identity());
    }


    public static ComplexityScore tallyToComplexityScore(LongTally tally, double baseline, Function<Double, Double> valueTransformer) {
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
                .rawValue(tally.count())
                .score(score)
                .baseline(baseline)
                .build();
    }

}
