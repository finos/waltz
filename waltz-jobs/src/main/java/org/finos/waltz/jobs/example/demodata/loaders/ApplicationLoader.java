package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.schema.tables.records.ApplicationRecord;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;

public class ApplicationLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLoader.class);


    public static void process(DSLContext waltz, Sheet apps, Timestamp now) {
        Map<String, Long> ouExtIdToIdMap = waltz
                .select(ou.EXTERNAL_ID, ou.ID)
                .from(ou)
                .fetchMap(ou.EXTERNAL_ID, ou.ID);

        int insertCount = summarizeResults(StreamSupport
                .stream(apps.spliterator(), false)
                .skip(1)
                .map(row -> {
                    ApplicationRecord r = waltz.newRecord(app);
                    r.setName(strVal(row, Columns.B));
                    r.setDescription(strVal(row, Columns.D));
                    r.setAssetCode(strVal(row, Columns.A));
                    r.setOrganisationalUnitId(ouExtIdToIdMap.getOrDefault(strVal(row, Columns.C), 0L));
                    r.setOverallRating("A");
                    r.setProvenance(PROVENANCE);
                    r.setUpdatedAt(now);
                    r.setCreatedAt(now);
                    r.setBusinessCriticality("A");

                    return r;
                })
                .collect(collectingAndThen(
                        toSet(),
                        waltz::batchStore))
                .execute());

        LOG.debug("Created {} new applications", insertCount);
    }

}
