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

package org.finos.waltz.service.person;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.person.search.PersonSearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.person.ImmutablePerson;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.person.PersonKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;
import static org.finos.waltz.common.StringUtilities.isEmpty;

@Service
public class PersonService {

    private final PersonDao personDao;
    private final PersonSearchDao personSearchDao;


    @Autowired
    public PersonService(PersonDao personDao, PersonSearchDao personSearchDao) {
        checkNotNull(personDao, "personDao must not be null");
        checkNotNull(personSearchDao, "personSearchDao must not be null");

        this.personDao = personDao;
        this.personSearchDao = personSearchDao;
    }


    public Person getByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find person without an employeeId");
        return personDao.getByEmployeeId(employeeId);
    }


    public Person getById(long id) {
        return personDao.getById(id);
    }


    public List<Person> findDirectsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return time("PS.findDirectsByEmployeeId", () -> personDao.findDirectsByEmployeeId(employeeId));
    }


    public Set<Person> findDirectsForPersonIds(List<Long> personIds) {
        if (CollectionUtilities.isEmpty(personIds)) {
            return Collections.emptySet();
        }
        return time("PS.findDirectsByPersonIds", () -> personDao.findDirectsForPersonIds(personIds));
    }


    /**
     * Returned in order, immediate manager first
     **/
    public List<Person> findAllManagersByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return time("PS.findAllManagersByEmployeeId", () -> personDao.findAllManagersByEmployeeId(employeeId));
    }


    public List<Person> search(String query) {
        if (isEmpty(query)) return emptyList();
        return search(EntitySearchOptions.mkForEntity(EntityKind.PERSON, query));
    }


    public List<Person> search(EntitySearchOptions options) {
        return personSearchDao.search(options);
    }


    public List<Person> all() {
        return personDao.all();
    }


    public int[] bulkSave(List<ImmutablePerson> people) {
        return personDao.bulkSave(people);
    }


    public Person getPersonByUserId(String userId) {
        return personDao.getByUserEmail(userId);
    }


    public Map<PersonKind, Integer> countAllUnderlingsByKind(String employeeId){
        return personDao.countAllUnderlingsByKind(employeeId);
    }


    public Set<Person> findByEmployeeIds(Set<String> empIds) {
        return personDao.findByEmployeeIds(empIds);
    }


    public Set<Person> findActivePeopleByEmails(Set<String> emails){
        return personDao.findActivePeopleByEmails(emails);
    }

}
