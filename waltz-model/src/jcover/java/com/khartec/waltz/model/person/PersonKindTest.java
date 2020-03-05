package com.khartec.waltz.model.person;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.person.PersonKind
 *
 * @author Diffblue JCover
 */

public class PersonKindTest {

    @Test
    public void valuesReturnsEMPLOYEECONTRACTORCONSULTANT() {
        PersonKind[] result = PersonKind.values();
        assertThat(result[0], is(PersonKind.EMPLOYEE));
        assertThat(result[1], is(PersonKind.CONTRACTOR));
        assertThat(result[2], is(PersonKind.CONSULTANT));
    }
}
