package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

public class FunctionUtilities_discardResultTest {
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
