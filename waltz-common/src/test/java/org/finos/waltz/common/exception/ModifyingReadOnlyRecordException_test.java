package org.finos.waltz.common.exception;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ModifyingReadOnlyRecordException_test {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void modifyingReadOnlyRecordExceptionMessage() throws Exception {
        expectedEx.expect(ModifyingReadOnlyRecordException.class);
        expectedEx.expectMessage("Modifying Read Only Record");

        // do something that should throw the exception...
        System.out.println("=======Starting Exception process=======");
        throw new ModifyingReadOnlyRecordException("503","Modifying Read Only Record");
    }
}
