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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ListUtilities.concat;
import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toMap;


public class InvolvementGenerator {


    private static final Random rnd = new Random();


    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> developers = getEmployeeIdsByTitle(dsl, "%Developer%");
        List<String> managers = getEmployeeIdsByTitle(dsl, "%Manager%");
        List<String> analysts = getEmployeeIdsByTitle(dsl, "%Analyst%");
        List<String> administrators = getEmployeeIdsByTitle(dsl, "%Administrator%");
        List<String> qa = getEmployeeIdsByTitle(dsl, "%QA%");
        List<String> directors = getEmployeeIdsByTitle(dsl, "%Director%");;

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
                        "RANDOM_GENERATOR",
                        Long.valueOf(rnd.nextInt(13) + 1)))
                .collect(Collectors.toList());

        List<InvolvementRecord> ouSponsors = orgUnitIds.stream()
                .map(id -> new InvolvementRecord(
                        EntityKind.ORG_UNIT.name(),
                        id,
                        randomPick(directors),
                        "RANDOM_GENERATOR",
                        Long.valueOf(rnd.nextInt(13) + 1)))
                .collect(Collectors.toList());

        dsl.delete(INVOLVEMENT).execute();

        dsl.batchInsert(devInvolvements).execute();
        dsl.batchInsert(qaInvolvements).execute();
        dsl.batchInsert(supportManagerInvolvments).execute();
        dsl.batchInsert(projectManagerInvolvements).execute();
        dsl.batchInsert(analystInvolvments).execute();
        dsl.batchInsert(ouArchitects).execute();
        dsl.batchInsert(ouSponsors).execute();


        System.out.println("Done");

    }

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
                        "RANDOM_GENERATOR",
                        kindId));
    }



    private static EntityReference toAppRef(Long id) {
        return ImmutableEntityReference.builder()
                .id(id)
                .kind(EntityKind.APPLICATION)
                .build();
    }

}
