package com.khartec.waltz.model.command;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.command.CommandOutcome
 *
 * @author Diffblue JCover
 */

public class CommandOutcomeTest {

    @Test
    public void valuesReturnsSUCCESSFAILURE() {
        CommandOutcome[] result = CommandOutcome.values();
        assertThat(result[0], is(CommandOutcome.SUCCESS));
        assertThat(result[1], is(CommandOutcome.FAILURE));
    }
}
