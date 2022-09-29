package org.finos.waltz.service.report_grid;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.finos.waltz.service.report_grid.ReportGridUtilities.parseTableData;
import static org.junit.jupiter.api.Assertions.*;

public class ReportGridUtilitiesTest {

    private static final String HEADER_TEXT = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |";
    private static final String FILTER_INFO_TEXT = "| Filter Column | Filter Operator | Value/s |";

    private final String TEST_STRING = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` | `ORG_UNIT` | `1` |\n" +
            "\n" +
            "\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "| `Asset Kind/Application` | `CONTAINS_ANY_OPTION` | `PROVIDED` |\n" +
            "| `Developer` | `CONTAINS_ANY_OPTION` | `PROVIDED` |";

    private final String NO_SPACED_TABLES = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` | `ORG_UNIT` | `1` |\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "| `Asset Kind/Application` | `CONTAINS_ANY_OPTION` | `PROVIDED` |\n" +
            "| `Developer` | `CONTAINS_ANY_OPTION` | `PROVIDED` |";

    private final String TOO_MANY_HEADERS = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` | `ORG_UNIT` | `1` | `12` |\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "| `Asset Kind/Application` | `CONTAINS_ANY_OPTION` | `PROVIDED` |\n" +
            "| `Developer` | `CONTAINS_ANY_OPTION` | `PROVIDED` |";

    private final String NOT_ENOUGH_HEADERS = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` |\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "| `Asset Kind/Application` | `CONTAINS_ANY_OPTION` | `PROVIDED` |\n" +
            "| `Developer` | `CONTAINS_ANY_OPTION` | `PROVIDED` |";

    private final String TOO_MANY_FILTER_COLS = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` | `ORG_UNIT` | `1` |\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "| `Asset Kind/Application` | `CONTAINS_ANY_OPTION` | `PROVIDED` |\n" +
            "| `Developer` | `CONTAINS_ANY_OPTION` | `PROVIDED` | `PROVIDED`";

    private final String NOT_ENOUGH_FILTER_COLS = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- |\n" +
            "| `Grid Name` | `{GRIDEXTID}` | `ORG_UNIT` | `1` |\n" +
            "| Filter Column | Filter Operator | Value/s |\n" +
            "| --- | --- |\n" +
            "|` Asset Kind/Application` | `PROVIDED` |\n" +
            "| `Developer` |";

    private final String[] TEST_STRING_ARRAY = TEST_STRING.split("\\r?\\n");

    @Test
    public void emptyStringReturnsNull() {
        Object object = ReportGridUtilities.parseGridFilterNoteText("");
        assertNull(object);
    }

    @Test
    public void nullStringReturnsNull() {
        Object object = ReportGridUtilities.parseGridFilterNoteText(null);
        assertNull(object);
    }

    @Test
    public void parseEmptyTextShouldReturnEmptyList() {
        List<List<String>> cellList = parseTableData(null, HEADER_TEXT);
        assertEquals(0, cellList.size());
    }

    @Test
    public void parseNullTextShouldReturnEmptyList() {
        List<List<String>> cellList = parseTableData(null, HEADER_TEXT);
        assertEquals(0, cellList.size());
    }

    @Test
    public void parseNoteTextForHeaderShouldReturnOnlyOneRow() {
        List<List<String>> cellList = parseTableData(TEST_STRING_ARRAY, HEADER_TEXT);
        assertEquals(1, cellList.size());
    }

    @Test
    public void parseNoteTextForFiltersShouldReturnTwoRows() {
        List<List<String>> cellList = parseTableData(TEST_STRING_ARRAY, FILTER_INFO_TEXT);
        assertEquals(2, cellList.size());
    }

    @Test
    public void parseNoteTextShouldHaveFourFieldsInGridInfoRow() {
        Tuple2<List<String>, List<List<String>>> noteText = ReportGridUtilities.parseGridFilterNoteText(TEST_STRING);
        assertEquals(4, noteText.v1.size(), "Should have four fields of grid info in header row");
    }

    @Test
    public void parseNoteTextShouldHaveTwoFieldsInGridInfoRow() {
        Tuple2<List<String>, List<List<String>>> noteText = ReportGridUtilities.parseGridFilterNoteText(TEST_STRING);
        assertEquals(3, noteText.v2.get(0).size(), "Should have two fields in each filter info row");
    }

    @Test
    public void parseNoteTextShouldThrowErrorIfTablesNotCorrectlySpaced() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReportGridUtilities.parseGridFilterNoteText(NO_SPACED_TABLES),
                "Should ensure tables are correctly spaced");
    }

    @Test
    public void parseNoteTextShouldThrowErrorIfTooManyHeaderColumns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReportGridUtilities.parseGridFilterNoteText(TOO_MANY_HEADERS),
                "Should be 4 header columns");
    }

    @Test
    public void parseNoteTextShouldThrowErrorIfNotEnoughHeaderColumns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReportGridUtilities.parseGridFilterNoteText(NOT_ENOUGH_HEADERS),
                "Should be 4 header columns");
    }

    @Test
    public void parseNoteTextShouldThrowErrorIfTooManyFilterOptionColumns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReportGridUtilities.parseGridFilterNoteText(TOO_MANY_FILTER_COLS),
                "Should be 4 filter columns");
    }

    @Test
    public void parseNoteTextShouldThrowErrorIfNotEnoughFilterOptionColumns() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReportGridUtilities.parseGridFilterNoteText(NOT_ENOUGH_FILTER_COLS),
                "Should be 4 filter columns");
    }
}
