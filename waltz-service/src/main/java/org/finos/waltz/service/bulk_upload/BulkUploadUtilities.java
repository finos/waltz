package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.ArrayUtilities;
import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.StringUtilities;
import org.jooq.lambda.tuple.Tuple2;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.ArrayUtilities.isEmpty;
import static org.finos.waltz.common.StringUtilities.safeTrim;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class BulkUploadUtilities {

    public static Stream<Tuple2<Integer, String[]>> streamRowData(String inputString) {

        AtomicInteger lineNumber = new AtomicInteger(1);

        return IOUtilities.streamLines(new ByteArrayInputStream(inputString.getBytes()))
                .filter(StringUtilities::notEmpty)
                .filter(r -> !r.startsWith("#"))
                .map(r -> {
                    String delimiters = "[,\\t|]+";
                    return r.split(delimiters);
                })
                .map(r -> tuple(lineNumber.getAndIncrement(), r));
    }

    public static Set<String> getColumnValuesFromInputString(String inputString, int columnOffset) {
        if (columnOffset < 0) {
            throw new IndexOutOfBoundsException("Cannot return a value for a negative column offset");
        }
        return getColumnValuesFromRowStream(streamRowData(inputString), columnOffset);
    }

    public static Set<String> getColumnValuesFromRows(Set<Tuple2<Integer, String[]>> rows, int columnOffset) {
        if (columnOffset < 0) {
            throw new IndexOutOfBoundsException("Cannot return a value for a negative column offset");
        }
        return getColumnValuesFromRowStream(rows.stream(), columnOffset);
    }

    private static Set<String> getColumnValuesFromRowStream(Stream<Tuple2<Integer, String[]>> rows, int columnOffset) {
        return rows
                .filter(Objects::nonNull)
                .filter(t -> !ArrayUtilities.isEmpty(t.v2))
                .filter(t -> t.v2.length > columnOffset) // prevent lookup where row is not wide enough for column lookup
                .map(t -> {
                    String[] cells = t.v2();
                    String cell = cells[columnOffset];
                    return safeTrim(cell);
                })
                .filter(StringUtilities::notEmpty)
                .collect(Collectors.toSet());
    }

}
