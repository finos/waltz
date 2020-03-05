package com.khartec.waltz.model.usage_info;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

/**
 * Unit tests for com.khartec.waltz.model.usage_info.UsageKind
 *
 * @author Diffblue JCover
 */

public class UsageKindTest {

    @Test
    public void isReadOnly() {
        assertThat(UsageKind.MODIFIER.isReadOnly(), is(false));
        assertThat(UsageKind.CONSUMER.isReadOnly(), is(true));
    }

    @Test
    public void valuesReturnsCONSUMERDISTRIBUTORMODIFIERORIGINATOR() {
        UsageKind[] result = UsageKind.values();
        assertThat(result[0], is(UsageKind.CONSUMER));
        assertThat(result[1], is(UsageKind.DISTRIBUTOR));
        assertThat(result[2], is(UsageKind.MODIFIER));
        assertThat(result[3], is(UsageKind.ORIGINATOR));
    }
}
