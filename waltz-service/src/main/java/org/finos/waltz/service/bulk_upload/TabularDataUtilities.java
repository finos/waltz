package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.StringUtilities;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.finos.waltz.common.ArrayUtilities.idx;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.containsDuplicates;
import static org.finos.waltz.common.MapUtilities.indexBy;
import static org.finos.waltz.common.StringUtilities.isEmpty;
import static org.finos.waltz.service.bulk_upload.BulkUploadUtilities.streamRowData;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class TabularDataUtilities {

    public static final class Row {

        private final TabularRow inputRow;
        private final Map<String, Integer> colIdxByName;


        public Row(TabularRow inputRow, Map<String, Integer> colIdxByName) {
            this.inputRow = inputRow;
            this.colIdxByName = colIdxByName;
        }

        public String getValue(String name) {
            Integer colIdx = colIdxByName.get(name);
            return idx(inputRow.values(), colIdx, null);
        }

        public Integer getRowNum() {
            return inputRow.rowNumber();
        }

        public Set<String> getHeaders() {
            return colIdxByName.keySet();
        }
    }

    public static Stream<Row> streamData(String inputString) {

        if (isEmpty(inputString)) {
            throw new IllegalStateException("Cannot parse empty data string");
        }
        AtomicInteger colIdx = new AtomicInteger(0);

        List<String> headers = streamRowData(inputString)
                .findFirst()
                .map(r -> Arrays
                        .stream(r.values())
                        .map(StringUtilities::safeTrim)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new IllegalStateException("Has no header row"));

        if (containsDuplicates(headers)) {
            throw new IllegalStateException("Duplicate header columns provided");
        }

        Map<String, Integer> colIdxByName = indexBy(headers, d -> d, d -> colIdx.getAndIncrement());

        return streamRowData(inputString)
                .skip(1)
                .map(r -> new Row(r, colIdxByName));
    }
}
