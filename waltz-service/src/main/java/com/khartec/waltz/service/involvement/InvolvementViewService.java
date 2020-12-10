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

package com.khartec.waltz.service.involvement;


import com.khartec.waltz.model.involvement.ImmutableInvolvementViewItem;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.involvement.InvolvementViewItem;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.person.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.common.SetUtilities.map;
import static java.util.stream.Collectors.toSet;

@Service
public class InvolvementViewService {

    private final InvolvementService involvementService;
    private final PersonService personService;

    @Autowired
    public InvolvementViewService(InvolvementService involvementService,
                                  PersonService personService){

        this.involvementService = involvementService;
        this.personService = personService;
    }


    public Set<InvolvementViewItem> findAllByEmployeeId(String employeeId) {

        List<Involvement> involvements = involvementService.findAllByEmployeeId(employeeId);


        Set<String> employeeIds = map(involvements, Involvement::employeeId);


        Set<Person> involvedPeople = personService.findByEmployeeIds(employeeIds);


        Map<String, Person> peopleByEmployeeId = indexBy(involvedPeople, Person::employeeId);

        return involvements
                .stream()
                .map(d -> {
                    Person person = peopleByEmployeeId.getOrDefault(d.employeeId(), null);

                    if (person == null) {return null;}

                    return mkInvolvementViewItem(d, person);
                })
                .filter(Objects::nonNull)
                .collect(toSet());
    }


    private InvolvementViewItem mkInvolvementViewItem(Involvement involvement, Person person) {
        return ImmutableInvolvementViewItem.builder()
                .involvement(involvement)
                .person(person)
                .build();
    }
}
