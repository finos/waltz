package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FunctionUtilities_alwaysTest {
    @Test
    public void getBiForNull(){
        Object output = FunctionUtilities.alwaysBi(null);
        assertNotNull(output);
    }

    @Test
    public void getBiForEmpty(){
        Object output = FunctionUtilities.alwaysBi("");
        assertNotNull(output);
    }

    @Test
    public void getBiForResult(){
        Object output = FunctionUtilities.alwaysBi("a");
        assertNotNull(output);
    }
}
