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

package com.khartec.waltz.model.scenario;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChangeScenarioCommandTest {

    @Test
    public void canDetermineIfRatingHasChanged() {
        assertTrue(hasChanged('a', 'b'));
        assertTrue(hasChanged('a', null));
        assertTrue(hasChanged(null, 'b'));
        assertFalse(hasChanged(null, null));
        assertFalse(hasChanged('a', 'a'));
    }


    private boolean hasChanged(Character a, Character b) {
        return mkCommand(a, b)
                .hasRatingChanged();
    }


    private ImmutableChangeScenarioCommand mkCommand(Character a, Character b) {
        return ImmutableChangeScenarioCommand.builder()
                .appId(1)
                .columnId(1)
                .rowId(1)
                .scenarioId(1)
                .ratingSchemeId(1)
                .rating(a)
                .previousRating(b)
                .build();
    }
}