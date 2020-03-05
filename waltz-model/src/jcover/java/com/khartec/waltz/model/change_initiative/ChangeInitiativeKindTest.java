package com.khartec.waltz.model.change_initiative;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.change_initiative.ChangeInitiativeKind
 *
 * @author Diffblue JCover
 */

public class ChangeInitiativeKindTest {

    @Test
    public void valuesReturnsINITIATIVEPROGRAMMEPROJECT() {
        ChangeInitiativeKind[] result = ChangeInitiativeKind.values();
        assertThat(result[0], is(ChangeInitiativeKind.INITIATIVE));
        assertThat(result[1], is(ChangeInitiativeKind.PROGRAMME));
        assertThat(result[2], is(ChangeInitiativeKind.PROJECT));
    }
}
