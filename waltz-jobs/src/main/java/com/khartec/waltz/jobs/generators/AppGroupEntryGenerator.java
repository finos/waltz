package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.schema.tables.records.ApplicationGroupEntryRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.Tables.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;

public class AppGroupEntryGenerator implements SampleDataGenerator {


    Random rnd = new Random();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);
        List<Long> groupIds = dsl
                .select(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .fetch(APPLICATION_GROUP.ID);


        List<ApplicationGroupEntryRecord> records = groupIds
                .stream()
                .flatMap(id -> IntStream
                        .range(0, rnd.nextInt(20) + 5)
                        .mapToLong(idx -> randomPick(appIds))
                        .distinct()
                        .mapToObj(appId -> {
                            ApplicationGroupEntryRecord record = dsl.newRecord(APPLICATION_GROUP_ENTRY);
                            record.setGroupId(id);
                            record.setApplicationId(appId);
                            return record;
                        }))
                .collect(Collectors.toList());

        dsl.batchStore(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        // handled in appgroup
        return false;
    }
}
