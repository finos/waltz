package org.finos.waltz.model.scenario;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeScenarioCommandTest {

    @Test
    public void canDetermineIfRatingHasChanged() {
        assertTrue(hasChanged("a", "b"));
        assertTrue(hasChanged("a", null));
        assertTrue(hasChanged(null, "b"));
        assertFalse(hasChanged(null, null));
        assertFalse(hasChanged("a", "a"));
    }


    private boolean hasChanged(String a, String b) {
        return mkCommand(a, b)
                .hasRatingChanged();
    }


    private ImmutableChangeScenarioCommand mkCommand(String a, String b) {
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