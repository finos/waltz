package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.schema.tables.records.OrganisationalUnitRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static org.finos.waltz.common.StringUtilities.isDefined;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class OrgUnitLoader {

    private static final Logger LOG = LoggerFactory.getLogger(OrgUnitLoader.class);

    public static void process(DSLContext waltz,
                               Sheet orgSheet,
                               Timestamp now) {
        AtomicLong idProvider = new AtomicLong(1);
        Set<Tuple2<OrganisationalUnitRecord, String>> recordsWithParentIds = StreamSupport
                .stream(orgSheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    OrganisationalUnitRecord r = waltz.newRecord(ou);
                    r.setId(idProvider.getAndIncrement());
                    r.setName(strVal(row, Columns.B));
                    r.setDescription(strVal(row, Columns.D));
                    r.setExternalId(strVal(row, Columns.A));
                    r.setProvenance("Waltz");
                    r.setLastUpdatedAt(now);
                    r.setCreatedAt(now);
                    r.setCreatedBy(USER_ID);
                    r.setLastUpdatedBy(USER_ID);
                    return tuple(r, strVal(row, Columns.C));
                })
                .collect(Collectors.toSet());

        int insertCount = summarizeResults(
                waltz
                    .batchInsert(SetUtilities.map(
                            recordsWithParentIds,
                            t -> t.v1))
                    .execute());

        Map<String, OrganisationalUnitRecord> extIdToOuMap = waltz
                .selectFrom(ou)
                .fetchMap(ou.EXTERNAL_ID);

        int updateCount = summarizeResults(
                recordsWithParentIds
                        .stream()
                        .filter(t -> isDefined(t.v2))
                        .map(t -> t.concat(extIdToOuMap.get(t.v2)))
                        .map(t -> waltz
                                .update(ou)
                                .set(ou.PARENT_ID, t.v3.getId())
                                .where(ou.EXTERNAL_ID.eq(t.v1.getExternalId())))
                        .collect(collectingAndThen(Collectors.toSet(), waltz::batch))
                        .execute());

        LOG.debug("Created {} org units and updated {} parent ids", insertCount, updateCount);
    }


}
