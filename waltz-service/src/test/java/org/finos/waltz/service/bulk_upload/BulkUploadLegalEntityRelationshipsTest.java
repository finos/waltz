package org.finos.waltz.service.bulk_upload;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.service.bulk_upload.BulkUploadLegalEntityRelationshipService.readRows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BulkUploadLegalEntityRelationshipsTest {

    private static final String SIMPLE_TEST_STRING = "App Id, Legal Entity Id, Comment\n" +
            "12345, ABCD, TestComment";

    private static final String INCLUDING_EMPTY_STRING = "App Id, , Comment\n" +
            "12345, ABCD, TestComment";

    @Test
    public void nullStringReturnsNull() {
        assertThrows(
                IllegalStateException.class,
                () -> readRows(null));
    }

    @Test
    public void returnsListPerNewLine() {
        Tuple2<Tuple2<Integer, String[]>, Set<Tuple2<Integer, String[]>>> resolvedRows = readRows(SIMPLE_TEST_STRING);
        assertEquals(1, resolvedRows.v2.size(), "Should return a list of string per new line in original input");
    }

    @Test
    public void returnsListStringPerLineDeterminedByCommaSeparation() {
        Tuple2<Tuple2<Integer, String[]>, Set<Tuple2<Integer, String[]>>> resolvedRows = readRows(SIMPLE_TEST_STRING);
        Tuple2<Integer, String[]> row = first(resolvedRows.v2);
        assertEquals(3, row.v2.length, "Should break string on commas to form list of data");
    }

    @Test
    public void returnsArrayIncludingEmptyElementsWhereNoData() {
        Tuple2<Tuple2<Integer, String[]>, Set<Tuple2<Integer, String[]>>> resolvedRows = readRows(INCLUDING_EMPTY_STRING);
        Tuple2<Integer, String[]> row = first(resolvedRows.v2);
        assertEquals(3, row.v2.length, "Should break string on commas to form list of data");
    }

}
