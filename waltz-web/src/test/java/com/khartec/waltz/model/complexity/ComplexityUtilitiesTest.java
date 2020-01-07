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