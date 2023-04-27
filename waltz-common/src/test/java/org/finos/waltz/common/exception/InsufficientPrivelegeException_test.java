package org.finos.waltz.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InsufficientPrivelegeException_test {


    @Test
    public void insufficientPrivelegeExceptionMessage() throws Exception {
        Throwable exception = assertThrows(InsufficientPrivelegeException.class, () -> {
            System.out.println("=======Starting Exception process=======");
            throw new InsufficientPrivelegeException("Insuffcient Privilege");
        });
        assertEquals(exception.getMessage(), "Insuffcient Privilege");

    }
}
