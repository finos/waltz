/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ListUtilities.concat;
import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.Tables.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toMap;


public class InvolvementGenerator implements SampleDataGenerator {


    private static final Random rnd = RandomUtilities.getRandom();


    private static List<Long> getAppIdsByKind(DSLContext dsl, ApplicationKind kind) {
        return dsl.select(APPLICATION.ID).from(APPLICATION).where(APPLICATION.KIND.eq(kind.name())).fetch(APPLICATION.ID);
    }

    private static List<String> getEmployeeIdsByTitle(DSLContext dsl, String title) {
        return dsl.select(PERSON.EMPLOYEE_ID).from(PERSON).where(PERSON.TITLE.like(title)).fetch(PERSON.EMPLOYEE_ID);
    }

    private static Stream<InvolvementRecord> mkInvolvments(EntityReference appRef, List<String> employeeIds, long kindId, int upperBound) {
        int count = rnd.nextInt(upperBound) + 1;
        return IntStream.range(0, count)
                .mapToObj(i -> new InvolvementRecord(
                        appRef.kind().name(),
                        appRef.id(),
                        randomPick(employeeIds),
                        SAMPLE_DATA_PROVENANCE,
                        kindId,
                        true));
    }



    private static EntityReference toAppRef(Long id) {
        return ImmutableEntityReference.builder()
                .id(id)
                .kind(EntityKind.APPLICATION)
                .build();
    }

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> developers = getEmployeeIdsByTitle(dsl, "%Developer%");
        List<String> managers = getEmployeeIdsByTitle(dsl, "%Manager%");
        List<String> analysts = getEmployeeIdsByTitle(dsl, "%Analyst%");
        List<String> administrators = getEmployeeIdsByTitle(dsl, "%Administrator%");
        List<String> qa = getEmployeeIdsByTitle(dsl, "%QA%");
        List<String> directors = getEmployeeIdsByTitle(dsl, "%Director%");

        List<Long> orgUnitIds = dsl.select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(ORGANISATIONAL_UNIT.ID);

        Map<String, Long> involvementKindMap = dsl.select(INVOLVEMENT_KIND.NAME, INVOLVEMENT_KIND.ID)
                .from(INVOLVEMENT_KIND)
                .fetch()
                .stream()
                .collect(toMap(r -> r.getValue(INVOLVEMENT_KIND.NAME), r -> r.getValue(INVOLVEMENT_KIND.ID)));

        List<Long> inHouseApps = getAppIdsByKind(dsl, ApplicationKind.IN_HOUSE);
        List<Long> hostedApps = getAppIdsByKind(dsl, ApplicationKind.INTERNALLY_HOSTED);
        List<Long> externalApps = getAppIdsByKind(dsl, ApplicationKind.EXTERNALLY_HOSTED);
        List<Long> eucApps = getAppIdsByKind(dsl, ApplicationKind.EUC);

        List<InvolvementRecord> devInvolvements = inHouseApps.stream()
                .map(id -> toAppRef(id))
                .flatMap(appRef -> mkInvolvments(appRef, developers, involvementKindMap.get("Developer"), 7))
                .collect(Collectors.toList());

        List<InvolvementRecord> qaInvolvements = concat(inHouseApps, hostedApps)
                .stream()
                .map(id -> toAppRef(id))
                .flatMap(appRef -> mkInvolvments(appRef, qa, involvementKindMap.get("Quality Assurance"), 3))
                .collect(Collectors.toList());

        List<InvolvementRecord> projectManagerInvolvements = concat(inHouseApps, externalApps, hostedApps, eucApps)
                .stream()
                .map(id -> toAppRef(id))
                .flatMap(appRef -> mkInvolvments(appRef, managers, involvementKindMap.get("Project Manager"), 1))
                .collect(Collectors.toList());

        List<InvolvementRecord> supportManagerInvolvments = concat(inHouseApps, externalApps, hostedApps)
                .stream()
                .map(id -> toAppRef(id))
                .flatMap(appRef -> mkInvolvments(appRef, managers, involvementKindMap.get("Support Manager"), 1))
                .collect(Collectors.toList());

        List<InvolvementRecord> analystInvolvments = concat(inHouseApps, externalApps, hostedApps)
                .stream()
                .map(id -> toAppRef(id))
                .flatMap(appRef -> mkInvolvments(appRef, analysts, involvementKindMap.get("Business Analyst"), 3))
                .collect(Collectors.toList());

        List<InvolvementRecord> ouArchitects = orgUnitIds.stream()
                .map(id -> new InvolvementRecord(
                        EntityKind.ORG_UNIT.name(),
                        id,
                        randomPick(directors),
                        SAMPLE_DATA_PROVENANCE,
                        Long.valueOf(rnd.nextInt(13) + 1),
                        true))
                .collect(Collectors.toList());

        List<InvolvementRecord> ouSponsors = orgUnitIds.stream()
                .map(id -> new InvolvementRecord(
                        EntityKind.ORG_UNIT.name(),
                        id,
                        randomPick(directors),
                        SAMPLE_DATA_PROVENANCE,
                        Long.valueOf(rnd.nextInt(13) + 1),
                        true))
                .collect(Collectors.toList());

        List<InvolvementRecord> categoryInvolvements = dsl
                .select(MEASURABLE_CATEGORY.ID)
                .from(MEASURABLE_CATEGORY)
                .fetch(MEASURABLE_CATEGORY.ID)
                .stream()
                .map(id -> new InvolvementRecord(
                        EntityKind.MEASURABLE_CATEGORY.name(),
                        id,
                        randomPick(directors),
                        SAMPLE_DATA_PROVENANCE,
                        Long.valueOf(rnd.nextInt(13) + 1),
                        true))
                .collect(Collectors.toList());

        dsl.batchInsert(devInvolvements).execute();
        dsl.batchInsert(qaInvolvements).execute();
        dsl.batchInsert(supportManagerInvolvments).execute();
        dsl.batchInsert(projectManagerInvolvements).execute();
        dsl.batchInsert(analystInvolvments).execute();
        dsl.batchInsert(ouArchitects).execute();
        dsl.batchInsert(ouSponsors).execute();
        dsl.batchInsert(categoryInvolvements).execute();


        System.out.println("Done");
        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.in(
                        EntityKind.APPLICATION.name(),
                        EntityKind.ORG_UNIT.name()))
                .and(INVOLVEMENT.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        return true;
    }
}
