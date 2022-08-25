package org.finos.waltz.service.report_grid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.finos.waltz.service.report_grid.ReportGridUtilities.parseTableData;
import static org.junit.jupiter.api.Assertions.*;

public class ReportGridUtilitiesTest {

    private final String TEST_STRING = "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |\n" +
            "| --- | --- | --- | --- | \n" +
            "| Grid Name | {GRIDEXTID} | ORG_UNIT | 1 |\n" +
            "\n" +
            "\n" +
            "| Filter Column | Values |\n" +
            "| --- | --- | \n" +
            "| Kind | In House; Third Party | \n" +
            "| Lifecycle Phase | Production |";

    private final String[] TEST_STRING_ARRAY = TEST_STRING.split("\\r?\\n");

    @Test
    public void emptyStringReturnsNull() {
        Object object = ReportGridUtilities.parseNoteText("");
        assertNull(object);
    }

    @Test
    public void nullStringReturnsNull() {
        Object object = ReportGridUtilities.parseNoteText(null);
        assertNull(object);
    }

    @Test
    public void parseEmptyTextShouldReturnEmptyList() {
        ArrayList<List<String>> cellList = parseTableData(null, "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |");
        assertEquals(0, cellList.size());
    }

    @Test
    public void parseNullTextShouldReturnEmptyList() {
        ArrayList<List<String>> cellList = parseTableData(null, "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |");
        assertEquals(0, cellList.size());
    }

    @Test
    public void parseNoteTextForHeaderShouldReturnOnlyOneRow() {
        ArrayList<List<String>> cellList = parseTableData(TEST_STRING_ARRAY, "| Grid Name | Grid Identifier | Vantage Point Kind | Vantage Point Id |");
        assertEquals(1, cellList.size());
    }

    @Test
    public void parseNoteTextForFiltersShouldReturnTwoRows() {
        ArrayList<List<String>> cellList = parseTableData(TEST_STRING_ARRAY, "| Filter Column | Values |");
        assertEquals(2, cellList.size());
    }

    @Test
    public void parseNoteText() {
        System.out.println("running");
        ReportGridUtilities.parseNoteText(TEST_STRING);
    }

}
