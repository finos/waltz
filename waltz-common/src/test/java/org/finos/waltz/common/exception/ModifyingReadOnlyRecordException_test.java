package org.finos.waltz.common.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ModifyingReadOnlyRecordException_test {


    @Test
    public void modifyingReadOnlyRecordExceptionMessage() throws Exception {
        Throwable exception = assertThrows(ModifyingReadOnlyRecordException.class, () -> {
            System.out.println("=======Starting Exception process=======");
            throw new ModifyingReadOnlyRecordException("503", "Modifying Read Only Record");
        });
        assertEquals(exception.getMessage(), "Modifying Read Only Record");
    }
}
