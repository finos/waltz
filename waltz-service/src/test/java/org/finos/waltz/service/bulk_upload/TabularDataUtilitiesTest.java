package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.SetUtilities.asSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TabularDataUtilitiesTest {

    @Test
    public void emptyInputIsRejected() {
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData(null));
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData(""));
    }


    @Test
    public void duplicateHeadersAreRejected() {
        assertThrows(IllegalStateException.class, () -> TabularDataUtilities.streamData("a,b,a\n1,2,3"));
    }


    @Test
    public void headersAreTrimmedAndRowsExposedByColumnName() {
        List<Row> rows = TabularDataUtilities
                .streamData(" name , kind \nfoo,APPLICATION\nbar,ACTOR")
                .collect(Collectors.toList());

        assertEquals(2, rows.size());

        Row first = rows.get(0);
        assertEquals(asSet("name", "kind"), first.getHeaders());
        assertEquals("foo", first.getValue("name"));
        assertEquals("APPLICATION", first.getValue("kind"));
        assertEquals(2, first.getRowNum());

        Row second = rows.get(1);
        assertEquals("bar", second.getValue("name"));
        assertEquals("ACTOR", second.getValue("kind"));
        assertEquals(3, second.getRowNum());
    }


    @Test
    public void unknownColumnGivesNull() {
        Row row = TabularDataUtilities
                .streamData("a,b\n1,2")
                .findFirst()
                .get();

        assertNull(row.getValue("c"));
    }


    @Test
    public void shortRowsGiveNullForMissingCells() {
        Row row = TabularDataUtilities
                .streamData("a,b,c\n1,2")
                .findFirst()
                .get();

        assertEquals("1", row.getValue("a"));
        assertEquals("2", row.getValue("b"));
        assertNull(row.getValue("c"));
    }


    @Test
    public void tabAndPipeDelimitersAreSupported() {
        Row tabRow = TabularDataUtilities.streamData("a\tb\n1\t2").findFirst().get();
        assertEquals("2", tabRow.getValue("b"));

        Row pipeRow = TabularDataUtilities.streamData("a|b\n1|2").findFirst().get();
        assertEquals("2", pipeRow.getValue("b"));
    }


    @Test
    public void headerOnlyInputGivesNoRows() {
        assertEquals(0, TabularDataUtilities.streamData("a,b").count());
    }

}
