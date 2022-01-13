package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectUtilities_dumpTest {

    @Test
    public void simpleDump(){
        int result = ObjectUtilities.dump(1);
        assertEquals(1, result);
    }

    @Test
    public void simpleDumpEmptyString(){
        String result = ObjectUtilities.dump("");
        assertEquals("", result);
    }

    @Test
    public void simpleDumpNullString(){
        String result = ObjectUtilities.dump(null);
        assertEquals(null, result);
    }
}
