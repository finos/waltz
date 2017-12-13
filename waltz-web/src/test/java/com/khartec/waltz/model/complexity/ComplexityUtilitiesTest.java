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