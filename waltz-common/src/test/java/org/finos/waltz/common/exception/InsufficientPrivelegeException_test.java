package org.finos.waltz.common.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InsufficientPrivelegeException_test {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void insufficientPrivelegeExceptionMessage() throws Exception {
        expectedEx.expect(InsufficientPrivelegeException.class);
        expectedEx.expectMessage("Insuffcient Privelege");

        // do something that should throw the exception...
        System.out.println("=======Starting Exception process=======");
        throw new InsufficientPrivelegeException("Insuffcient Privelege");
    }
}
