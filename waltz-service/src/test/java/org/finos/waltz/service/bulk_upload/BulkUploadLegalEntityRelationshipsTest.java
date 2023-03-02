package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.service.bulk_upload.BulkUploadUtilities.streamRowData;
import static org.finos.waltz.service.bulk_upload.TabularDataUtilities.streamData;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.util.CollectionUtils.isEmpty;

public class BulkUploadLegalEntityRelationshipsTest {

    private static final String SIMPLE_TEST_STRING = "App Id, Legal Entity Id, Comment\n" +
            "12345, ABCD, TestComment";

    private static final String DUPLICATE_HEADER_STRING = "App Id, App Id, Comment\n" +
            "12345, ABCD, TestComment";

    private static final String INCLUDING_EMPTY_STRING = "App Id, , Comment\n" +
            "12345, ABCD, TestComment";

    @Test
    public void throwsExceptionWhenNoDataProvided() {
        assertThrows(
                IllegalStateException.class,
                () -> streamData(""));
    }

    @Test
    public void throwsExceptionWhenNullDataProvided() {
        assertThrows(
                IllegalStateException.class,
                () -> streamData(null));
    }

    @Test
    public void throwsExceptionWhenDuplicateHeader() {
        assertThrows(
                IllegalStateException.class,
                () -> streamData(DUPLICATE_HEADER_STRING));
    }

    @Test
    public void returnsListPerNewLine() {
        Set<Row> resolvedRows = streamData(SIMPLE_TEST_STRING).collect(Collectors.toSet());
        assertEquals(1, resolvedRows.size(), "Should return a list of string per new line in original input");
    }

    @Test
    public void returnsListStringPerLineDeterminedByCommaSeparation() {
        Set<TabularRow> resolvedRows = streamRowData(SIMPLE_TEST_STRING).collect(Collectors.toSet());
        TabularRow row = first(resolvedRows);
        assertEquals(3, row.values().length, "Should break string on commas to form list of data");
    }

    @Test
    public void returnsArrayIncludingEmptyElementsWhereNoData() {
        Set<TabularRow> resolvedRows = streamRowData(INCLUDING_EMPTY_STRING).collect(Collectors.toSet());
        TabularRow row = first(resolvedRows);
        assertEquals(3, row.values().length, "Should break string on commas to form list of data");
    }

    @Test
    public void getColumnValuesFromInputString() {

        assertThrows(IndexOutOfBoundsException.class,
                () -> BulkUploadUtilities.getColumnValuesFromInputString(SIMPLE_TEST_STRING, -1),
                "Should throw an error if asking to return a column with a negative offset value");

        Set<String> columnNotIncluded = BulkUploadUtilities.getColumnValuesFromInputString(SIMPLE_TEST_STRING, 16);
        assertTrue(isEmpty(columnNotIncluded), "Should return empty set if column index is out of bounds");

        Set<String> columns = BulkUploadUtilities.getColumnValuesFromInputString(SIMPLE_TEST_STRING, 0);
        assertEquals(asSet("App Id", "12345"), columns, "Should return correct values for column offset");
    }

}
