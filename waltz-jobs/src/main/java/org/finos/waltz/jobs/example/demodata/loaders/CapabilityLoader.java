package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.schema.tables.records.MeasurableRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.StringUtilities.isDefined;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;

public class CapabilityLoader {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilityLoader.class);


    public static void process(DSLContext waltz,
                               Sheet capSheet,
                               Long mcId,
                               Timestamp now) {

        Set<MeasurableRecord> records = StreamSupport
                .stream(capSheet.spliterator(), false)
                .skip(1)
                .map(row -> {
                    MeasurableRecord r = waltz.newRecord(m);
                    r.setName(strVal(row, Columns.B));
                    r.setDescription(strVal(row, Columns.D));
                    r.setExternalId(strVal(row, Columns.A));
                    r.setExternalParentId(strVal(row, Columns.C));
                    r.setProvenance(PROVENANCE);
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(USER_ID);
                    r.setMeasurableCategoryId(mcId);
                    return r;
                })
                .collect(toSet());

        int insertCount = summarizeResults(waltz.batchStore(records).execute());

        Map<String, MeasurableRecord> extToRecordMap = waltz.selectFrom(m).fetchMap(m.EXTERNAL_ID);

        int updateCount = summarizeResults(
                extToRecordMap
                        .values()
                        .stream()
                        .filter(r -> isDefined(r.getExternalParentId()))
                        .map(r -> waltz
                                .update(m)
                                .set(m.PARENT_ID, extToRecordMap.get(r.getExternalParentId()).getId())
                                .where(m.ID.eq(r.getId())))
                        .collect(collectingAndThen(toSet(), waltz::batch))
                        .execute());

        LOG.debug(
                "Created {} measurables in category {}, then updated {} parentIds",
                insertCount,
                mcId,
                updateCount);
    }

}
