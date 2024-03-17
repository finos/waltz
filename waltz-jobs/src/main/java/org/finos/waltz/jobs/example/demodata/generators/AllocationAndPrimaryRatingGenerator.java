package org.finos.waltz.jobs.example.demodata.generators;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.schema.tables.records.AllocationRecord;
import org.finos.waltz.schema.tables.records.AllocationSchemeRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.finos.waltz.common.RandomUtilities.randomIntBetween;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.jobs.example.demodata.DemoUtils.*;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class AllocationAndPrimaryRatingGenerator {

    private static final String SAMPLE_ALLOC_SCHEME = "SAMPLE_ALLOC_SCHEME";


    public static void generate(DSLContext dsl, Long mcId) {
        String mcName = dsl
                .select(mc.NAME)
                .from(mc)
                .where(mc.ID.eq(mcId))
                .fetchOne(mc.NAME);

        Long schemeId = createAllocScheme(dsl, mcId, mcName);

        enablePrimaryRatings(dsl, mcId);

        Set<Tuple3<Long, Integer, Boolean>> ratingAllocsAndPrimaryFlag = prepareRatingsAllocsAndPrimaryFlags(dsl, mcId);

        updatePrimaryFlags(dsl, ratingAllocsAndPrimaryFlag);
        createAllocs(dsl, schemeId, ratingAllocsAndPrimaryFlag);
    }


    private static void createAllocs(DSLContext dsl,
                                     Long schemeId,
                                     Set<Tuple3<Long, Integer, Boolean>> ratingAllocsAndPrimaryFlag) {
        Timestamp now = DateTimeUtilities.nowUtcTimestamp();

        ratingAllocsAndPrimaryFlag
                .stream()
                .map(t -> {
                    AllocationRecord allocationRecord = dsl.newRecord(alloc);
                    allocationRecord.setMeasurableRatingId(t.v1);
                    allocationRecord.setAllocationPercentage(t.v2);
                    allocationRecord.setLastUpdatedAt(now);
                    allocationRecord.setLastUpdatedBy(USER_ID);
                    allocationRecord.setProvenance(PROVENANCE);
                    allocationRecord.setAllocationSchemeId(schemeId);
                    return allocationRecord;
                })
                .collect(Collectors.collectingAndThen(Collectors.toSet(), dsl::batchInsert))
                .execute();
    }


    private static void updatePrimaryFlags(DSLContext dsl,
                                           Set<Tuple3<Long, Integer, Boolean>> ratingAllocsAndPrimaryFlag) {
        ratingAllocsAndPrimaryFlag
                .stream()
                .filter(t -> t.v3)
                .map(t -> dsl
                        .update(mr)
                        .set(mr.IS_PRIMARY, true)
                        .where(mr.ID.eq(t.v1)))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), dsl::batch))
                .execute();
    }


    private static Set<Tuple3<Long, Integer, Boolean>> prepareRatingsAllocsAndPrimaryFlags(DSLContext dsl,
                                                                                           Long mcId) {
        Map<Long, List<Long>> ratingsByEntity = dsl
                .select(mr.ENTITY_ID, mr.ID)
                .from(mr)
                .where(mr.MEASURABLE_ID.in(DSL
                        .select(m.ID)
                        .from(m)
                        .where(m.MEASURABLE_CATEGORY_ID.eq(mcId))))
                .fetchGroups(mr.ENTITY_ID, mr.ID);

        return ratingsByEntity
                .entrySet()
                .stream()
                .flatMap(kv -> {
                    List<Long> ratings = kv.getValue();

                    int maxInitialAlloc = 100 / ratings.size();
                    List<Tuple2<Long, Integer>> randomAllocs = ListUtilities.map(
                            ratings,
                            r -> tuple(r, randomIntBetween(0, maxInitialAlloc)));
                    int remainingAllocation = 100 - randomAllocs
                            .stream()
                            .mapToInt(t -> t.v2)
                            .sum();

                    Long primaryRating = randomPick(randomAllocs).v1;
                    return randomAllocs
                            .stream()
                            .map(t -> t.v1 == primaryRating
                                    ? t.map2(currentAlloc -> currentAlloc + remainingAllocation).concat(true)
                                    : t.concat(false));
                })
                .collect(Collectors.toSet());
    }


    private static void enablePrimaryRatings(DSLContext dsl,
                                             Long mcId) {
        dsl.update(mc)
                .set(mc.ALLOW_PRIMARY_RATINGS, true)
                .where(mc.ID.eq(mcId))
                .execute();
    }


    private static Long createAllocScheme(DSLContext dsl,
                                          Long mcId,
                                          String mcName) {
        AllocationSchemeRecord allocSchemeRecord = dsl.newRecord(allocScheme);
        allocSchemeRecord.setMeasurableCategoryId(mcId);
        allocSchemeRecord.setName(format("%s Allocation", mcName));
        allocSchemeRecord.setDescription(format("%s Allocation Scheme", mcName));
        allocSchemeRecord.setExternalId(format("%s_%d", SAMPLE_ALLOC_SCHEME, mcId));
        allocSchemeRecord.store();

        Long schemeId = allocSchemeRecord.getId();
        return schemeId;
    }


}
