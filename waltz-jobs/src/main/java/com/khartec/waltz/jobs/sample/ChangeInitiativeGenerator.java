/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeRecord;
import com.khartec.waltz.schema.tables.records.EntityRelationshipRecord;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.TableRecord;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.model.EntityKind.APP_GROUP;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toList;

public class ChangeInitiativeGenerator {

    private static final Random rnd = new Random();


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



    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> groupIds = dsl.select(APPLICATION_GROUP.ID)
                .from(APPLICATION_GROUP)
                .fetch(APPLICATION_GROUP.ID);

        List<String> employeeIds = dsl
                .select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .fetch(PERSON.EMPLOYEE_ID);

        List<Long> orgUnitIds = dsl.select(ORGANISATIONAL_UNIT.ID)
                    .from(ORGANISATIONAL_UNIT)
                    .fetch(ORGANISATIONAL_UNIT.ID);

        List<TableRecord<?>> records = LongStream.range(0, 400)
                .mapToObj(i -> {
                    String name = randomPick(p1)
                            + " "
                            + randomPick(p2)
                            + " "
                            + randomPick(p3);
                    Long ouId = randomPick(orgUnitIds.toArray(new Long[0]));
                    return Tuple.tuple(i, name, ouId);
                })
                .map(t -> buildChangeInitiativeRecord(t))
                .flatMap(r -> Stream.concat(Stream.of(r), buildLinks(r, groupIds, employeeIds)))
                .collect(toList());


        System.out.println("-- deleting");
        dsl.deleteFrom(CHANGE_INITIATIVE).execute();
        System.out.println("-- inserting");
        dsl.batchInsert(records).execute();
        System.out.println(" -- done");


    }

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
                    record.setEmployeeId(ListUtilities.randomPick(employeeIds));
                    return record;
                });
    }

    private static Stream<TableRecord<?>> maybeBuildGroupLink(ChangeInitiativeRecord r, List<Long> groupIds) {
        if (rnd.nextInt(100) < 5) {
            EntityRelationshipRecord record = new EntityRelationshipRecord();
            record.setKindA(APP_GROUP.name());
            record.setIdA(ListUtilities.randomPick(groupIds));
            record.setKindB(EntityKind.CHANGE_INITIATIVE.name());
            record.setIdB(r.getId());
            record.setRelationship(RelationshipKind.RELATES_TO.name());
            return Stream.of(record);
        } else {
            return Stream.empty();
        }
    }

    private static ChangeInitiativeRecord buildChangeInitiativeRecord(Tuple3<Long, String, Long> t) {
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
        record.setOrganisationalUnitId(t.v3);
        record.setEndDate(new Date(
                Instant.now()
                    .plusSeconds(rnd.nextInt(60 * 60 * 24 * 365 * 2))
                    .toEpochMilli()));
        return record;

    }

}
