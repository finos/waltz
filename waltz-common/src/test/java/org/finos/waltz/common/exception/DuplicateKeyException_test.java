package org.finos.waltz.common.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DuplicateKeyException_test {

    @Test
    public void duplicateKeyExceptionMessage1() throws Exception {
        Throwable exception = assertThrows(DuplicateKeyException.class, () -> {
            System.out.println("=======Starting Exception process=======");
            throw new DuplicateKeyException("Duplicate key added");
        });
        assertEquals(exception.getMessage(), "Duplicate key added");
    }
}
