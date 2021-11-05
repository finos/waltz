package org.finos.waltz.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionUtilities_alwaysBi {
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
