package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.ArrayUtilities;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.service.bulk_upload.TabularDataUtilities.Row;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class BulkUploadUtilities {

    public static Stream<TabularRow> streamRowData(String inputString) {

        AtomicInteger lineNumber = new AtomicInteger(1);

        return IOUtilities.streamLines(new ByteArrayInputStream(inputString.getBytes()))
                .filter(StringUtilities::notEmpty)
                .filter(r -> !r.startsWith("#"))
                .map(r -> {
                    String delimiters = "[,\\t|]+";
                    return r.split(delimiters);
                })
                .map(r -> ImmutableTabularRow.builder()
                        .rowNumber(lineNumber.getAndIncrement())
                        .values(r)
                        .build());
    }

    public static Set<String> getColumnValuesFromInputString(String inputString, int columnOffset) {
        if (columnOffset < 0) {
            throw new IndexOutOfBoundsException("Cannot return a value for a negative column offset");
        }
        return getColumnValuesFromRowStreamByIndex(streamRowData(inputString), columnOffset);
    }

    public static Set<String> getColumnValuesFromRows(Set<Row> rows, String columnHeader) {
        return rows
                .stream()
                .filter(Objects::nonNull)
                .map(t -> t.getValue(columnHeader))
                .filter(StringUtilities::notEmpty)
                .collect(Collectors.toSet());
    }

    private static Set<String> getColumnValuesFromRowStreamByIndex(Stream<TabularRow> rows, int columnOffset) {
        return rows
                .filter(Objects::nonNull)
                .filter(t -> !ArrayUtilities.isEmpty(t.values()))
                .filter(t -> t.values().length > columnOffset) // prevent lookup where row is not wide enough for column lookup
                .map(t -> {
                    String[] cells = t.values();
                    String cell = cells[columnOffset];
                    return safeTrim(cell);
                })
                .filter(StringUtilities::notEmpty)
                .collect(Collectors.toSet());
    }

}
