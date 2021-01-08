package com.khartec.waltz.common.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UpdateFailedException_test {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void updateFailedExceptionMessage() throws Exception {
        expectedEx.expect(UpdateFailedException.class);
        expectedEx.expectMessage("Update Failed");

        // do something that should throw the exception...
        System.out.println("=======Starting Exception process=======");
        throw new UpdateFailedException("500","Update Failed");
    }
}
