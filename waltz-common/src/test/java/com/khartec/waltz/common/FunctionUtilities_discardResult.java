package com.khartec.waltz.common;

import org.junit.Test;

public class FunctionUtilities_discardResult {
    @Test
    public void discardNullResult(){
        FunctionUtilities.discardResult(null);
    }

    @Test
    public void discardEmptyResult(){
        FunctionUtilities.discardResult("");
    }

    @Test
    public void discardResult(){
        FunctionUtilities.discardResult("a");
    }
}
