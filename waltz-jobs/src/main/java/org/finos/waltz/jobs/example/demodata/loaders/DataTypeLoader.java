package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.schema.tables.records.DataTypeRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.StringUtilities.isDefined;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class DataTypeLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DataTypeLoader.class);

    public static void process(DSLContext waltz,
                                  Sheet dtSheet,
                                  Timestamp now) {

        AtomicLong idCtr = new AtomicLong(1L);
        Set<Tuple2<DataTypeRecord, String>> recordsAndParentExtIds = StreamSupport
                .stream(dtSheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    DataTypeRecord r = waltz.newRecord(dt);
                    r.setId(idCtr.getAndIncrement());
                    r.setCode(strVal(row, Columns.A));
                    r.setName(strVal(row, Columns.C));
                    r.setDescription(strVal(row, Columns.D));
                    r.setLastUpdatedAt(now);
                    return tuple(r, strVal(row, Columns.B));
                })
                .collect(toSet());

        int insertCount = summarizeResults(waltz
                .batchInsert(SetUtilities.map(recordsAndParentExtIds, t -> t.v1))
                .execute());

        Map<String, Long> dtToIdMap = fetchDataTypeExtIdToIdMap(waltz);

        int updCount = summarizeResults(recordsAndParentExtIds
                .stream()
                .filter(t -> isDefined(t.v2))
                .map(t -> tuple(t.v1.getId(), dtToIdMap.get(t.v2)))
                .filter(t -> t.v2 != null)
                .map(t -> waltz.update(dt).set(dt.PARENT_ID, t.v2).where(dt.ID.eq(t.v1)))
                .collect(collectingAndThen(toSet(), waltz::batch))
                .execute());

        LOG.debug(
                "Created {} new data type records and updated {} parent ids",
                insertCount,
                updCount);
    }

}
