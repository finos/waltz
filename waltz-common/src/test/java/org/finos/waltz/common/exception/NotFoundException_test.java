package org.finos.waltz.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotFoundException_test {


    @Test
    public void notFoundExceptionMessage() throws Exception {
        Throwable exception = assertThrows(NotFoundException.class, () -> {
            System.out.println("=======Starting Exception process=======");
            throw new NotFoundException("404", "Not Found");
        });
        assertEquals(exception.getMessage(), "Not Found");
    }
}
