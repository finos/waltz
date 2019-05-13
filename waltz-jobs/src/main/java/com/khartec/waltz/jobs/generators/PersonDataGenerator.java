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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.person.ImmutablePerson;
import com.khartec.waltz.model.person.PersonKind;
import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;


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
        getDsl(ctx).deleteFrom(PERSON).execute();
        return true;
    }


    private static void visit(ImmutablePerson parent, int level, List<Long> orgUnitIds) {

        if (level > MAX_DEPTH) return;

        int nextLevel = level + 1;

        int siblingCount = level == 1 ? SampleData.jobTitles[1].length : rnd.nextInt(7) + 2;

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
