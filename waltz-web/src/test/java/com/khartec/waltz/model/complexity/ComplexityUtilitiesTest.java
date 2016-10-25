package com.khartec.waltz.model.complexity;

import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import org.junit.Test;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;
import static org.junit.Assert.assertEquals;

/**
 * Created by dwatkins on 25/01/2016.
 */
public class ComplexityUtilitiesTest {

    private final Tally<Long> tally = ImmutableTally.<Long>builder().id(1L).count(10).build();


    @Test(expected = IllegalArgumentException.class)
    public void mustSupplyATally() {
        tallyToComplexityScore(ComplexityKind.CONNECTION, null, 10);
    }


    @Test(expected = IllegalArgumentException.class)
    public void negativeMaximumsAreIllegal() {
        tallyToComplexityScore(ComplexityKind.CONNECTION, tally, -1);
    }


    @Test
    public void maxOfZeroGivesAComplexityOfZero() {
        ComplexityScore complexityScore = tallyToComplexityScore(ComplexityKind.CONNECTION, tally, 0);
        assertEquals(0, complexityScore.score(), 0);
    }


    @Test
    public void aTallyEqualToMaxShouldGiveComplexityScoreOfOne() {
        ComplexityScore complexityScore = tallyToComplexityScore(ComplexityKind.CONNECTION, tally, 10);
        assertEquals(1, complexityScore.score(), 0);
    }


    @Test
    public void aTallyEqualToHalfOfMaxShouldGiveComplexityScoreOfPointFive() {
        ComplexityScore complexityScore = tallyToComplexityScore(
                ComplexityKind.CONNECTION,
                tally,
                20);
        assertEquals(0.5, complexityScore.score(), 0);
    }


    @Test
    public void negativeCountGivesNegativeScore() {
        Tally<Long> negativeTally = ImmutableTally.<Long>builder().id(1L).count(-10).build();
        ComplexityScore complexityScore = tallyToComplexityScore(ComplexityKind.CONNECTION, negativeTally, 10);
        assertEquals(-1, complexityScore.score(), 0);
    }

}