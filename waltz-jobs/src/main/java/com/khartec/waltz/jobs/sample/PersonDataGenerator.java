/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.person.ImmutablePerson;
import com.khartec.waltz.model.person.PersonKind;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.person.PersonService;
import com.khartec.waltz.service.person_hierarchy.PersonHierarchyService;
import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.jobs.sample.SampleData.departmentNames;
import static com.khartec.waltz.jobs.sample.SampleData.jobTitles;


public class PersonDataGenerator {

    private static final int MAX_DEPTH = 6;

    private static final List<ImmutablePerson> peeps = new ArrayList<>();
    private static final Fairy fairy = Fairy.create();

    public static void main(String[] args) {


        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        PersonService personService = ctx.getBean(PersonService.class);
        PersonHierarchyService personHierarchyService = ctx.getBean(PersonHierarchyService.class);

        Person person = fairy.person();

        ImmutablePerson root = ImmutablePerson.builder()
                .employeeId(person.passportNumber())
                .kind(PersonKind.EMPLOYEE)
                .userPrincipalName(person.username())
                .title(randomPick(jobTitles[0]))
                .departmentName("CEO")
                .displayName(person.fullName())
                .email(person.email())
                .build();


        peeps.add(root);

        visit(root, 1);


        System.out.println(peeps.size());

        personService.bulkSave(peeps);
        personHierarchyService.build();

    }


    private static final Random rnd = new Random();
    private static int counter = 0;

    private static void visit(ImmutablePerson parent, int level) {

        if (level > MAX_DEPTH) return;

        int nextLevel = level + 1;

        int siblingCount = level == 1 ? jobTitles[1].length : rnd.nextInt(7) + 2;

        for (int i = 0 ; i < siblingCount ; i++) {
            Person person = fairy.person();

            String jobTitle = level == 1
                    ? jobTitles[1][i]
                    : randomPick(
                        jobTitles[level >= jobTitles.length ? jobTitles.length - 1 : level]);

            ImmutablePerson p = ImmutablePerson.builder()
                    .managerEmployeeId(parent.employeeId())
                    .employeeId(person.passportNumber())
                    .kind(PersonKind.EMPLOYEE)
                    .userPrincipalName(person.username())
                    .title(jobTitle)
                    .departmentName(randomPick(departmentNames))
                    .displayName(person.fullName())
                    .email((counter++) + person.email())
                    .build();

            peeps.add(p);

            visit(p, nextLevel);


        }
    }

}
