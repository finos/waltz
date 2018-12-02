package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.MeasurableRatingRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.common.SetUtilities.uniqBy;
import static com.khartec.waltz.schema.Tables.MEASURABLE;
import static com.khartec.waltz.schema.Tables.MEASURABLE_RATING;

/**
 * Created by dwatkins on 04/03/2017.
 */
public class MeasurableRatingGenerator implements SampleDataGenerator {

    private final Random rnd = new Random();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);


        List<Long> appIds = getAppIds(dsl);
        List<Long> mIds = dsl
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.CONCRETE.isTrue())
                .fetch()
                .getValues(MEASURABLE.ID);



        List<MeasurableRatingRecord> records = appIds.stream()
                .flatMap(appId ->
                        IntStream.range(0, rnd.nextInt(MAX_RATINGS_PER_APP))
                                .mapToObj(idx -> Tuple.tuple(appId, randomPick(mIds))))
                .map(t -> {
                    MeasurableRatingRecord record = dsl.newRecord(MEASURABLE_RATING);
                    record.setEntityId(t.v1);
                    record.setEntityKind(EntityKind.APPLICATION.name());
                    record.setRating(ArrayUtilities.randomPick("R", "A", "G"));
                    record.setMeasurableId(t.v2);
                    record.setLastUpdatedBy("admin");
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    return record;
                })
                .collect(Collectors.toList());

        Set<MeasurableRatingRecord> dedupedRecords = uniqBy(
                records,
                r -> Tuple.tuple(r.getMeasurableId(), r.getEntityId()));

        dsl.batchStore(dedupedRecords).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }


}
