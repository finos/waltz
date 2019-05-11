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

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeRecord;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.DSLContext;
import org.jooq.TableRecord;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.model.EntityKind.APP_GROUP;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toList;

public class ChangeInitiativeGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    private static final String[] p1 = new String[] {
            "Change", "Enhance", "Deliver", "Adapt to", "Meet",
            "Invest in", "Perform", "Undertake", "Manage",
            "Analyze", "Restructure", "Lead", "Prioritise",
            "Reduce", "Lower"
    };

    private static final String[] p2 = new String[] {
            "Regulatory", "Compliance", "Market",
            "Global", "Regional", "Tactical", "Enterprise",
            "Industry", "Governance", "Auditor",
            "Business", "Customer"
    };

    private static final String[] p3 = new String[] {
            "Processes", "Standards", "Trends",
            "Initiatives", "Reporting", "Operations", "Aggregation",
            "Structures"
    };

    private static Stream<TableRecord<?>> buildLinks(ChangeInitiativeRecord r,
                                                     List<Long> groupIds,
                                                     List<String> employeeIds) {
        return Stream.concat(maybeBuildGroupLink(r, groupIds), buildPersonLinks(r, employeeIds));
    }

    private static Stream<InvolvementRecord> buildPersonLinks(ChangeInitiativeRecord r, List<String> employeeIds) {
        return IntStream.range(0, 5)
                .mapToObj(i -> {
                    InvolvementRecord record = new InvolvementRecord();
                    record.setKindId(Long.valueOf(rnd.nextInt(13) + 1));
                    record.setProvenance("dummy");
                    record.setEntityId(r.getId());
                    record.setEntityKind(EntityKind.CHANGE_INITIATIVE.name());
                    record.setEmployeeId(randomPick(employeeIds));
                    return record;
                });
    }

    private static Stream<TableRecord<?>> maybeBuildGroupLink(ChangeInitiativeRecord r, List<Long> groupIds) {
        if (rnd.nextInt(100) < 5 && groupIds.size() > 0) {
            EntityRelationshipRecord record = new EntityRelationshipRecord();
            record.setKindA(APP_GROUP.name());
            record.setIdA(randomPick(groupIds));
            record.setKindB(EntityKind.CHANGE_INITIATIVE.name());
            record.setIdB(r.getId());
            record.setRelationship(RelationshipKind.RELATES_TO.name());
            return Stream.of(record);
        } else {
            return Stream.empty();
        }
    }

    private static ChangeInitiativeRecord buildChangeInitiativeRecord(Tuple2<Long, String> t) {
        Date.from(Instant.now());
        ChangeInitiativeRecord record = new ChangeInitiativeRecord();
        record.setDescription(t.v2);
        record.setName(t.v2);
        record.setProvenance("dummy");
        record.setExternalId("EXT" + t.v1);
        record.setKind("PROGRAMME");
        record.setLifecyclePhase(randomPick(LifecyclePhase.values()).name());
        record.setId(t.v1);
        record.setStartDate(new Date(Instant.now().toEpochMilli()));
        record.setEndDate(new Date(
                Instant.now()
                    .plusSeconds(rnd.nextInt(60 * 60 * 24 * 365 * 2))
                    .toEpochMilli()));
        return record;

    }

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);

        List<Long> groupIds = dsl.select(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .fetch(APPLICATION_GROUP.ID);

        List<String> employeeIds = dsl
                .select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .fetch(PERSON.EMPLOYEE_ID);

        List<TableRecord<?>> records = LongStream.range(0, NUM_CHANGE_INITIATIVES)
                .mapToObj(i -> {
                    String name = randomPick(p1)
                            + " "
                            + randomPick(p2)
                            + " "
                            + randomPick(p3);
                    return Tuple.tuple(i, name);
                })
                .map(t -> buildChangeInitiativeRecord(t))
                .flatMap(r -> Stream.concat(Stream.of(r), buildLinks(r, groupIds, employeeIds)))
                .collect(toList());

        dsl.batchInsert(records).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        log("-- deleting");
        getDsl(ctx).deleteFrom(CHANGE_INITIATIVE).execute();
        return true;
    }
}
