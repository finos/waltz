package org.finos.waltz.jobs.example.demodata.loaders;

import org.apache.poi.ss.usermodel.Sheet;
import org.finos.waltz.common.Columns;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.jobs.example.demodata.InvestmentRating;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.StreamUtilities.mkSiphon;
import static org.finos.waltz.data.JooqUtilities.summarizeResults;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AppToCapabilityLoader {

    private static final Logger LOG = LoggerFactory.getLogger(AppToCapabilityLoader.class);


    public static void process(DSLContext waltz,
                               Sheet appToCapSheet,
                               Timestamp now) {

        Map<String, Long> appToIdMap = fetchAppExtIdToIdMap(waltz);
        Map<String, Long> capToIdMap = fetchCapabilityExtIdToIdMap(waltz);

        StreamUtilities.Siphon<Tuple4<String, String, Long, Long>> badRowSiphon = mkSiphon(t -> t.v3 == null || t.v4 == null);

        int insertCount = summarizeResults(StreamSupport
                .stream(appToCapSheet.spliterator(), false)
                .skip(1)
                .map(row -> tuple(
                        strVal(row, Columns.A),
                        strVal(row, Columns.B)))
                .map(t -> tuple(
                        t.v1,
                        t.v2,
                        appToIdMap.get(t.v1),
                        capToIdMap.get(t.v2)))
                .filter(badRowSiphon)
                .map(t -> {
                    InvestmentRating rating = randomPick(
                            InvestmentRating.INVEST,
                            InvestmentRating.INVEST,
                            InvestmentRating.MAINTAIN,
                            InvestmentRating.MAINTAIN,
                            InvestmentRating.MAINTAIN,
                            InvestmentRating.DISINVEST);

                    MeasurableRatingRecord r = waltz.newRecord(mr);
                    r.setRating(rating.code());
                    r.setEntityKind(EntityKind.APPLICATION.name());
                    r.setEntityId(t.v3);
                    r.setMeasurableId(t.v4);
                    r.setProvenance(PROVENANCE);
                    r.setLastUpdatedAt(now);
                    r.setLastUpdatedBy(USER_ID);
                    return r;
                })
                .collect(collectingAndThen(toSet(), waltz::batchInsert))
                .execute());

        LOG.debug("Created {} app to capability mappings", insertCount);


        if (badRowSiphon.hasResults()) {
            LOG.info("Bad rows: {}", badRowSiphon.getResults());
        }
    }
}
