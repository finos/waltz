package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionUtilities_alwaysBiTest {
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
