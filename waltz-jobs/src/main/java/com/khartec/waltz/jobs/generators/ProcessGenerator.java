/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import org.jooq.Batch;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;

public class ProcessGenerator implements SampleDataGenerator {

    private static final String[] p1 = new String[] {
            "Client", "Accounting", "Regulatory",
            "Customer", "Financial", "Market",
            "Industry", "Vertical", "Horizontal"
    };

    private static final String[] p2 = new String[] {
            "Onboarding", "Processing", "Reporting",
            "Discovery", "Tracking", "Monitoring",
            ""
    };


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        long category = getCategory(dsl);


        System.out.println("Setting up processes in category: " + category);
        setupMeasurables(dsl, category);
        return null;
    }


    private void setupMeasurables(DSLContext dsl, long category) {
        List<MeasurableRecord> records = new ArrayList<>();

        for (long g = 1; g <= NUM_PROCESS_GROUPS; g++ ) {
            System.out.println("mkGroup: " + g);
            MeasurableRecord record = dsl.newRecord(MEASURABLE);
            record.setDescription("Process Group: " + g);
            record.setName("Process Group " + g);
            record.setMeasurableCategoryId(category);
            record.setConcrete(false);
            record.setProvenance(SAMPLE_DATA_PROVENANCE);
            record.setLastUpdatedBy("admin");
            record.insert();

            long groupId = record.getId();

            for (long p = 0; p < NUM_PROCESSES_IN_GROUP; p++) {
                MeasurableRecord record2 = dsl.newRecord(MEASURABLE);
                String name = randomPick(p1)
                        + " "
                        + randomPick(p2);
                record2.setDescription("Process: " + name);
                record2.setName(name);
                record2.setParentId(groupId);
                record2.setMeasurableCategoryId(category);
                record2.setConcrete(true);
                record2.setProvenance(SAMPLE_DATA_PROVENANCE);
                record2.setLastUpdatedBy("admin");
                records.add(record2);
                System.out.print(".");
            }

        }
        int[] rcs = dsl.batchInsert(records).execute();
        System.out.println("\nBatch inserted: "+rcs.length);
    }


    @Override
    public boolean remove(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);
        long category = getCategory(dsl);

        Condition sampleMeasurableCondition = MEASURABLE.MEASURABLE_CATEGORY_ID.eq(category)
                .and(MEASURABLE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE));

        List<Long> mIds = dsl
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(sampleMeasurableCondition)
                .fetch()
                .map(r -> r.value1());

        dsl.deleteFrom(MEASURABLE_RATING)
                .where(MEASURABLE_RATING.MEASURABLE_ID.in(mIds));

        dsl.deleteFrom(MEASURABLE)
                .where(sampleMeasurableCondition)
                .execute();

        return true;
    }


    private long getCategory(DSLContext dsl) {
        return dsl.select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq("PROCESS"))
                .fetchOne()
                .value1();
    }
}
