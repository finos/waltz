package com.khartec.waltz.common.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NotFoundException_test {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void notFoundExceptionMessage() throws Exception {
        expectedEx.expect(NotFoundException.class);
        expectedEx.expectMessage("Not Found");

        // do something that should throw the exception...
        System.out.println("=======Starting Exception process=======");
        throw new NotFoundException("404","Not Found");
    }
}
