package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.runners.model.MultipleFailureException.assertEmpty;

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
