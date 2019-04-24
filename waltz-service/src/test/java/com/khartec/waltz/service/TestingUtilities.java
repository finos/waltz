package com.khartec.waltz.service;

import org.jooq.lambda.function.Consumer3;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.khartec.waltz.common.StringUtilities.lower;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class TestingUtilities {



    public static Consumer3<Runnable, String, String> assertThrows = (runnable, message, matchingText) -> {
        try {
            runnable.run();
            fail(message);
        } catch (Exception e) {
            if (matchingText != null) {
                assertTrue(
                        "Exception message should have contained: " + matchingText + " but was: "+e.getMessage(),
                        lower(e.getMessage()).contains(lower(matchingText)));
            }
        }
    };


    public static void assertSameValues(String message, ArrayList<BigDecimal> expected, List<BigDecimal> actual) {
        assertSameValues(message, expected, actual, 0);
    }


    public static void assertSameValues(String message, ArrayList<BigDecimal> expected, List<BigDecimal> actual, double tolerance) {
        if (expected.size() != actual.size()) {
            Assert.fail(message + ": expected list not same size as actual list");
        }
        BigDecimal toleranceDec = BigDecimal.valueOf(tolerance);

        for (int i = 0; i< expected.size(); i++) {
            BigDecimal expectedValue = expected.get(i);
            BigDecimal actualValue = actual.get(i);
            BigDecimal actualValueHigh = actualValue.add(toleranceDec);
            BigDecimal actualValueLow = actualValue.subtract(toleranceDec);

            assertTrue(String.format(
                    message + ": Expected value of [%s] is not equal to the actual value of [%s] at offset [%d] (with tolerance: %s)",
                    expectedValue,
                    actualValue,
                    i,
                    tolerance),
                    expectedValue.compareTo(actualValueHigh) <= 0 && expectedValue.compareTo(actualValueLow) >= 0);
        }
    }


}
