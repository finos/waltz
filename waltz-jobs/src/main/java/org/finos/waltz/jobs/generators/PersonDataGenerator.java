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

package org.finos.waltz.jobs.generators;

import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.Person;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.model.person.ImmutablePerson;
import org.finos.waltz.model.person.PersonKind;
import org.finos.waltz.service.person.PersonService;
import org.finos.waltz.service.person_hierarchy.PersonHierarchyService;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.schema.Tables.INVOLVEMENT;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.finos.waltz.schema.tables.Person.PERSON;


public class PersonDataGenerator implements SampleDataGenerator {

    private static final int MAX_DEPTH = 4;

    private static final List<ImmutablePerson> peeps = new ArrayList<>();
    private static final Fairy fairy = Fairy.create();

    private static int counter = 0;
    private static final Random rnd = RandomUtilities.getRandom();


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        PersonService personService = ctx.getBean(PersonService.class);
        PersonHierarchyService personHierarchyService = ctx.getBean(PersonHierarchyService.class);


        List<Long> ouIds = getDsl(ctx)
                .select(ORGANISATIONAL_UNIT.ID)
                .from(ORGANISATIONAL_UNIT)
                .fetch(ORGANISATIONAL_UNIT.ID);

        Person person = fairy.person();

        ImmutablePerson root = ImmutablePerson.builder()
                .employeeId(person.getPassportNumber())
                .personKind(PersonKind.EMPLOYEE)
                .userPrincipalName(person.getUsername())
                .title(randomPick(SampleData.jobTitles[0]))
                .departmentName("CEO")
                .displayName(person.getFullName())
                .email(person.getEmail())
                .organisationalUnitId(10L)
                .isRemoved(false)
                .build();


        peeps.add(root);

        visit(root, 1, ouIds);

        personService.bulkSave(peeps);

        ImmutablePerson admin = randomPick(peeps);

        getDsl(ctx)
                .update(PERSON)
                .set(PERSON.USER_PRINCIPAL_NAME, "admin")
                .set(PERSON.EMAIL, "admin")
                .where(PERSON.EMPLOYEE_ID.eq(admin.employeeId()))
                .execute();

        personHierarchyService.build();

        return MapUtilities.newHashMap("created", peeps.size());
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        log("Removing people");
        getDsl(ctx).deleteFrom(INVOLVEMENT).execute();
        getDsl(ctx).deleteFrom(PERSON).execute();
        return true;
    }


    private static void visit(ImmutablePerson parent, int level, List<Long> orgUnitIds) {

        if (level > MAX_DEPTH) return;

        int nextLevel = level + 1;

        int siblingCount = level == 1 ? SampleData.jobTitles[1].length : rnd.nextInt(4) + 2;

        for (int i = 0 ; i < siblingCount ; i++) {
            Person person = fairy.person();

            String jobTitle = level == 1
                    ? SampleData.jobTitles[1][i]
                    : randomPick(
                        SampleData.jobTitles[level >= SampleData.jobTitles.length ? SampleData.jobTitles.length - 1 : level]);

            Long ouId = orgUnitIds.get(rnd.nextInt(orgUnitIds.size()));

            ImmutablePerson p = ImmutablePerson.builder()
                    .managerEmployeeId(parent.employeeId())
                    .employeeId(person.getPassportNumber())
                    .personKind(PersonKind.EMPLOYEE)
                    .userPrincipalName(person.getUsername())
                    .title(jobTitle)
                    .departmentName(randomPick(SampleData.departmentNames))
                    .displayName(person.getFullName())
                    .email((counter++) + person.getEmail())
                    .organisationalUnitId(ouId)
                    .isRemoved(false)
                    .build();

            peeps.add(p);

            visit(p, nextLevel, orgUnitIds);
        }
    }


}
