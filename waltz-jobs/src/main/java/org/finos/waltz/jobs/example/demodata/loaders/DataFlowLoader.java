package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import org.finos.waltz.schema.tables.records.LogicalFlowRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple6;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class DataFlowLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DataFlowLoader.class);


    public static void process(DSLContext waltz,
                                  Sheet flowSheet,
                                  Timestamp now) {

        Map<String, Long> appToIdMap = fetchAppExtIdToIdMap(waltz);
        Map<String, Long> dtToIdMap = fetchDataTypeExtIdToIdMap(waltz);
        StreamUtilities.Siphon<Tuple6<String, String, String, Long, Long, Long>> badRowSiphon = mkSiphon(t -> t.v4 == null || t.v5 == null || t.v6 == null);

        Set<Tuple3<Long, Long, Long>> resolvedData = StreamSupport
                .stream(flowSheet.spliterator(), false)
                .skip(1)
                .map(row -> tuple(strVal(row, Columns.A), strVal(row, Columns.B), strVal(row, Columns.C)))
                .map(t -> t.concat(tuple(appToIdMap.get(t.v1), appToIdMap.get(t.v2), dtToIdMap.get(t.v3))))
                .filter(badRowSiphon)
                .map(Tuple6::skip3)
                .collect(toSet());

        int lfInsertCount = summarizeResults(resolvedData
                .stream()
                .map(Tuple3::limit2)
                .distinct()
                .map(t -> {
                    LogicalFlowRecord r = waltz.newRecord(lf);
                    r.setSourceEntityId(t.v1);
                    r.setTargetEntityId(t.v2);
                    r.setSourceEntityKind(EntityKind.APPLICATION.name());
                    r.setTargetEntityKind(EntityKind.APPLICATION.name());
                    r.setProvenance(PROVENANCE);
                    r.setLastUpdatedAt(now);
                    r.setCreatedAt(now);
                    r.setLastUpdatedBy(USER_ID);
                    r.setCreatedBy(USER_ID);
                    return r;
                })
                .collect(collectingAndThen(toSet(), waltz::batchInsert))
                .execute());

        Map<Tuple2<Long, Long>, Long> lfBySrcTarget = waltz
                .select(lf.SOURCE_ENTITY_ID, lf.TARGET_ENTITY_ID, lf.ID)
                .from(lf)
                .fetchMap(
                        r -> tuple(
                                r.get(lf.SOURCE_ENTITY_ID),
                                r.get(lf.TARGET_ENTITY_ID)),
                        r -> r.get(lf.ID));

        int lfdInsertCount = summarizeResults(resolvedData
                .stream()
                .map(t -> tuple(lfBySrcTarget.get(t.limit2()), t.v3)) // lfId, dtId
                .map(t -> {
                    LogicalFlowDecoratorRecord r = waltz.newRecord(lfd);
                    r.setLogicalFlowId(t.v1);
                    r.setDecoratorEntityId(t.v2);
                    r.setDecoratorEntityKind(EntityKind.DATA_TYPE.name());
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(USER_ID);
                    r.setProvenance(PROVENANCE);
                    return r;
                })
                .collect(collectingAndThen(toSet(), waltz::batchInsert))
                .execute());

        LOG.debug(
                "Created {} logical flows and {} decorations",
                lfInsertCount,
                lfdInsertCount);
    }

}
