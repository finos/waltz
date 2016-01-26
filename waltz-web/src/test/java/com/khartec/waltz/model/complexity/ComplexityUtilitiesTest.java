package com.khartec.waltz.model.complexity;

import com.khartec.waltz.model.tally.ImmutableLongTally;
import com.khartec.waltz.model.tally.LongTally;
import org.junit.Test;

import static com.khartec.waltz.model.complexity.ComplexityUtilities.tallyToComplexityScore;
import static org.junit.Assert.*;

/**
 * Created by dwatkins on 25/01/2016.
 */
public class ComplexityUtilitiesTest {

    private final LongTally tally = ImmutableLongTally.builder().id(1L).count(10).build();


    @Test(expected = IllegalArgumentException.class)
    public void mustSupplyATally() {
        tallyToComplexityScore(null, 10);
    }


    @Test(expected = IllegalArgumentException.class)
    public void negativeMaximumsAreIllegal() {
        tallyToComplexityScore(tally, -1);
    }


    @Test
    public void maxOfZeroGivesAComplexityOfZero() {
        ComplexityScore complexityScore = tallyToComplexityScore(tally, 0);
        assertEquals(0, complexityScore.score(), 0);
    }


    @Test
    public void aTallyEqualToMaxShouldGiveComplexityScoreOfOne() {
        ComplexityScore complexityScore = tallyToComplexityScore(tally, 10);
        assertEquals(1, complexityScore.score(), 0);
    }


    @Test
    public void aTallyEqualToHalfOfMaxShouldGiveComplexityScoreOfPointFive() {
        ComplexityScore complexityScore = tallyToComplexityScore(tally, 20);
        assertEquals(0.5, complexityScore.score(), 0);
    }


    @Test
    public void negativeCountGivesNegativeScore() {
        LongTally negativeTally = ImmutableLongTally.builder().id(1L).count(-10).build();
        ComplexityScore complexityScore = tallyToComplexityScore(negativeTally, 10);
        assertEquals(-1, complexityScore.score(), 0);
    }

}