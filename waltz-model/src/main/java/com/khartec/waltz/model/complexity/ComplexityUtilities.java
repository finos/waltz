package com.khartec.waltz.model.complexity;

import com.khartec.waltz.model.tally.LongTally;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;


public class ComplexityUtilities {

    public static ComplexityScore tallyToComplexityScore(LongTally tally, double baseline) {
        checkNotNull(tally, "Cannot create a complexity score from a null tally");
        checkTrue(baseline >= 0, "Cannot create a complexity score with a negative baseline value");

        double score = baseline == 0
                ? 0
                : tally.count() / baseline;

        return ImmutableComplexityScore.builder()
                .id(tally.id())
                .rawValue(tally.count())
                .score(score)
                .baseline(baseline)
                .build();
    }

}
