package org.finos.waltz.common;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class DebugUtilities_logValueFromResultFuncTest {

    @Test
    public void logNullMessage() {
        assertThrows(NullPointerException.class,
                () -> DebugUtilities.logValue(null, "aaa"));
    }

    @Test
    public void logNullResultAndMessage() {
        assertThrows(NullPointerException.class,
                () -> DebugUtilities.logValue(null, null));
    }

    @Test
    public void logResultAndMessage(){
        Supplier<Double> randomValue = Math::random;
        Double result = DebugUtilities.logValue(randomValue, 1);
        assertNotNull(result);
    }
}
