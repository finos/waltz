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