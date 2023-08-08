package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Columns_toOffsetTest {

    @Test
    public void toOffset() {
        assertEquals(Columns.A, Columns.toOffset("A"));
        assertEquals(Columns.BA, Columns.toOffset("BA"));
        assertEquals(53, Columns.toOffset("BB"));
    }
}
