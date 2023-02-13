package org.finos.waltz.service.bulk_upload;

import org.finos.waltz.common.IOUtilities;
import org.finos.waltz.common.StringUtilities;
import org.jooq.lambda.tuple.Tuple2;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.jooq.lambda.tuple.Tuple.tuple;

public class BulkUploadUtilities {

    public static Stream<Tuple2<Integer, String[]>> streamRowData(String inputString) {

        AtomicInteger lineNumber = new AtomicInteger(1);

        return IOUtilities.streamLines(new ByteArrayInputStream(inputString.getBytes()))
                .filter(StringUtilities::notEmpty)
                .filter(r -> !r.startsWith("#"))
                .map(r -> {
                    String delimiters = "[,;\\t|]+";
                    return r.split(delimiters);
                })
                .map(r -> tuple(lineNumber.getAndIncrement(), r));
    }

}
