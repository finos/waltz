package org.finos.waltz.common.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UpdateFailedException_test {


    @Test
    public void updateFailedExceptionMessage() throws Exception {
        Throwable exception = assertThrows(UpdateFailedException.class, () -> {
            System.out.println("=======Starting Exception process=======");
            throw new UpdateFailedException("500", "Update Failed");
        });
        assertEquals(exception.getMessage(), "Update Failed");
    }
}
