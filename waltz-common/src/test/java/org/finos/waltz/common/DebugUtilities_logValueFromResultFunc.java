package org.finos.waltz.common;

import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DebugUtilities_logValueFromResultFunc {
    @Test(expected = NullPointerException.class)
    public void logNullMessage(){
        String result = DebugUtilities.logValue(null, "aaa");
    }

    @Test(expected = NullPointerException.class)
    public void logNullResultAndMessage(){
        String result = DebugUtilities.logValue(null, null);
    }

    @Test
    public void logResultAndMessage(){
        Supplier<Double> randomValue = () -> Math.random();
        Double result = DebugUtilities.logValue(randomValue, 1);
        assertNotNull(result);
    }
}
