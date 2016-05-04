package com.khartec.waltz.common;

import org.junit.Test;

import static com.khartec.waltz.common.RangeBandUtilities.toPrettyString;
import static org.junit.Assert.assertEquals;

public class RangeBandUtilitiesTest {

    @Test
    public void looksPretty() {
        assertEquals("0 - *", toPrettyString(new RangeBand<>(0, null)));
        assertEquals("* - 10", toPrettyString(new RangeBand<>(null, 10)));
        assertEquals("0 - 10", toPrettyString(new RangeBand<>(0, 10)));
    }

}