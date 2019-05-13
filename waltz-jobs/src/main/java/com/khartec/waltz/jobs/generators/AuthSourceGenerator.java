package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.records.AuthoritativeSourceRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

/**
 * Created by dwatkins on 04/03/2017.
 */
public class AuthSourceGenerator implements SampleDataGenerator {


    private static final Random rnd = RandomUtilities.getRandom();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);

        List<Long> ouIds = dsl
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(ORGANISATIONAL_UNIT.ID);

        List<String> types = dsl
                .select(DATA_TYPE.CODE)
                .from(DATA_TYPE)
                .fetch(DATA_TYPE.CODE);

        List<AuthoritativeSourceRecord> records = types.stream()
                .flatMap(t -> IntStream
                        .range(0, 2 + rnd.nextInt(2))
                        .mapToObj(i -> {
                            AuthoritativeSourceRecord record = dsl.newRecord(AUTHORITATIVE_SOURCE);
                            record.setDataType(t);
                            record.setRating(randomPick(AuthoritativenessRating.PRIMARY, AuthoritativenessRating.SECONDARY).name());
                            record.setApplicationId(randomPick(appIds));
                            record.setParentId(randomPick(ouIds));
                            record.setParentKind(EntityKind.ORG_UNIT.name());
                            record.setProvenance(SAMPLE_DATA_PROVENANCE);
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(AUTHORITATIVE_SOURCE)
                .where(AUTHORITATIVE_SOURCE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }
}
