package org.finos.waltz.common.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DuplicateKeyException_test {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void duplicateKeyExceptionMessage1() throws Exception {
        expectedEx.expect(DuplicateKeyException.class);
        expectedEx.expectMessage("Duplicate key added");

        // do something that should throw the exception...
        System.out.println("=======Starting Exception process=======");
        throw new DuplicateKeyException("Duplicate key added");
    }
}
