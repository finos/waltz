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

package com.khartec.waltz.jobs.tools;

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.common.DebugUtilities;
import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.common.StreamUtilities.Siphon;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.person.PersonService;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.MapUtilities.countBy;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.StreamUtilities.mkSiphon;

public class InvolvementImporter {
    private static final ClassLoader classLoader = InvolvementImporter.class.getClassLoader();
    private static final String fileName = "involvement.tsv";


    private static String toSurnameFirstname(String s) {
        String[] bits = s.split(" +");
        String lastName = ArrayUtilities.last(bits);
        String[] firstNames = ArrayUtilities.initial(bits);
        return String.format(
                "%s, %s",
                lastName,
                String.join(" ", firstNames));
    }


    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);
        PersonService personService = ctx.getBean(PersonService.class);

        Map<String, Application> appsByExtId = indexBy(
                a -> a.assetCode().orElse(null),
                applicationService.findAll());

        Map<String, Person> peopleByName= indexBy(
                p -> toSurnameFirstname(p.displayName()).toLowerCase(),
                personService.all());

        Siphon<String[]> incorrectSizeSiphon = mkSiphon(arr -> arr.length != 3);
        Siphon<Tuple2<String ,?>> unknownAppSiphon = mkSiphon(t -> ! appsByExtId.containsKey(t.v1));
        Siphon<Tuple2<?, String>> unknownPersonSiphon = mkSiphon(t -> ! peopleByName.containsKey(t.v2.toLowerCase()));
        Siphon<Tuple2<Application ,?>> retiredAppSiphon = mkSiphon(t -> t.v1.lifecyclePhase() == LifecyclePhase.RETIRED);

        List<Tuple2<Application, Person>> r = IOUtilities
                .streamLines(classLoader.getResourceAsStream(fileName))
                .map(line -> line.split("\t"))
                .filter(d -> incorrectSizeSiphon.test(d))
                .map(arr -> Tuple.tuple(arr[1], arr[0]))
                .filter(t -> unknownAppSiphon.test(t))
                .map(t -> t.map1(extId -> appsByExtId.get(extId)))
                .filter(t -> retiredAppSiphon.test(t))
                .filter(t -> unknownPersonSiphon.test(t))
                .map(t -> t.map2(n -> peopleByName.get(n.toLowerCase())))
                .collect(Collectors.toList());

        Set<Application> distinctApps = r.stream().map(t -> t.v1).distinct().collect(Collectors.toSet());
        Set<Person> distinctPeople = r.stream().map(t -> t.v2).distinct().collect(Collectors.toSet());

        System.out.println("----");
        System.out.println("Wrong size count: "+ incorrectSizeSiphon.getResults().size());
        System.out.println("Apps not found count: "+ unknownAppSiphon.getResults().size());
        System.out.println("Retired app count: "+ retiredAppSiphon.getResults().size());
        System.out.println("Person not found count: "+ unknownPersonSiphon.getResults().size());
        System.out.println("----");
        System.out.println("Good record count: "+r.size());
        System.out.println("Distinct App count: "+distinctApps.size());
        System.out.println("Distinct People count: "+distinctPeople.size());

        Map<String, Long> unknownPersonCounts = countBy(Tuple2::v2, unknownPersonSiphon.getResults());
        DebugUtilities.dump(unknownPersonCounts);

    }

}
