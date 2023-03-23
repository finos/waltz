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

package org.finos.waltz.data.person;

import org.finos.waltz.schema.tables.records.PersonRecord;
import org.finos.waltz.model.person.ImmutablePerson;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.person.PersonKind;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectSeekStep1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.Tables.USER_ROLE;
import static org.finos.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static org.finos.waltz.common.Checks.checkNotEmpty;
import static org.finos.waltz.common.Checks.checkNotNull;


@Repository
public class PersonDao {

    private static final Logger LOG = LoggerFactory.getLogger(PersonDao.class);
    public static final RecordMapper<? super Record, Person> personMapper = r -> {
        PersonRecord record = r.into(PersonRecord.class);
        return ImmutablePerson.builder()
                .id(record.getId())
                .employeeId(record.getEmployeeId())
                .displayName(record.getDisplayName())
                .email(record.getEmail())
                .personKind(PersonKind.valueOf(record.getKind()))
                .userPrincipalName(Optional.ofNullable(record.getUserPrincipalName()))
                .departmentName(Optional.ofNullable(record.getDepartmentName()))
                .managerEmployeeId(Optional.ofNullable(record.getManagerEmployeeId()))
                .title(Optional.ofNullable(record.getTitle()))
                .mobilePhone(Optional.ofNullable(record.getMobilePhone()))
                .officePhone(Optional.ofNullable(record.getOfficePhone()))
                .organisationalUnitId(Optional.ofNullable(record.getOrganisationalUnitId()))
                .isRemoved(record.getIsRemoved())
                .build();
    };
    private final DSLContext dsl;


    @Autowired
    public PersonDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public Person getByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find person without an employeeId");
        return dsl.select()
                .from(PERSON)
                .where(PERSON.EMPLOYEE_ID.eq(employeeId))
                .fetchOne(personMapper);
    }


    public Person getById(long id) {
        return dsl.select()
                .from(PERSON)
                .where(PERSON.ID.eq(id))
                .fetchOne(personMapper);
    }


    /**
     * Finds Person by user email. If duplicates returns active one. Otherwise returns first.
     * @param email  email address of user
     * @return Person
     */
    public Person getByUserEmail(String email) {
        checkNotEmpty(email, "Cannot find person without a email");
        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.EMAIL.eq(email)) // TODO: change as part of 247
                .orderBy(PERSON.IS_REMOVED)
                .limit(1)
                .fetchOne(personMapper);
    }


    public Person getActiveByUserEmail(String email) {
        checkNotEmpty(email, "Cannot find person without a email");
        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.EMAIL.eq(email))
                .andNot(PERSON.IS_REMOVED)
                .fetchOne(personMapper);
    }


    public List<Person> findDirectsByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return dsl.select()
                .from(PERSON)
                .where(dsl.renderInlined(PERSON.MANAGER_EMPLOYEE_ID.eq(employeeId)))
                .andNot(PERSON.IS_REMOVED)
                .orderBy(PERSON.DISPLAY_NAME)
                .fetch(personMapper);
    }


    /**
     * Returned in order, immediate manager first
     **/
    public List<Person> findAllManagersByEmployeeId(String employeeId) {
        checkNotEmpty(employeeId, "Cannot find directs without an employeeId");
        return dsl.select(PERSON.fields())
                .from(PERSON)
                .join(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.MANAGER_ID))
                .where(PERSON_HIERARCHY.EMPLOYEE_ID.eq(employeeId))
                .andNot(PERSON.IS_REMOVED)
                .orderBy(PERSON_HIERARCHY.LEVEL.desc())
                .fetch(personMapper);
    }


    public List<Person> all() {
        return dsl.select()
                .from(PERSON)
                .where(PERSON.IS_REMOVED.eq(false))
                .fetch(personMapper);
    }


    public int[] bulkSave(List<ImmutablePerson> people) {

        checkNotNull(people, "Cannot bulk save a null collection of people");

        LOG.info("Bulk saving " + people.size() + "records");

        List<PersonRecord> records = people.stream()
                .map(p -> {
                    PersonRecord r = dsl.newRecord(PERSON);
                    r.setDisplayName(p.displayName());
                    r.setEmployeeId(p.employeeId());
                    r.setEmail(p.email());
                    r.setIsRemoved(p.isRemoved());
                    r.setKind(p.personKind().name());
                    r.setDepartmentName(p.departmentName().orElse(""));
                    r.setManagerEmployeeId(p.managerEmployeeId().orElse(""));
                    r.setTitle(p.title().orElse(""));
                    r.setOfficePhone(p.officePhone().orElse(""));
                    r.setMobilePhone(p.mobilePhone().orElse(""));
                    r.setUserPrincipalName(p.userPrincipalName().orElse(""));
                    r.setOrganisationalUnitId(p.organisationalUnitId().orElse(null));
                    return r;
                })
                .collect(Collectors.toList());

        return dsl.batchInsert(records).execute();
    }




    public List<Person> findPersonsByAttestationInstanceId(long instanceId) {
        return dsl.select(PERSON.fields())
                .from(ATTESTATION_INSTANCE_RECIPIENT)
                .innerJoin(PERSON)
                .on(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(PERSON.EMAIL))
                .where(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(instanceId))
                .andNot(PERSON.IS_REMOVED)
                .fetch(personMapper);
    }


    public Map<PersonKind, Integer> countAllUnderlingsByKind(String employeeId){

        Field<Integer> countField = DSL.count().as("count");

        return dsl
                .select(PERSON.KIND, countField)
                .from(PERSON_HIERARCHY)
                .innerJoin(PERSON)
                .on(PERSON_HIERARCHY.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                .andNot(PERSON.IS_REMOVED)
                .groupBy(PERSON.KIND)
                .fetchMap(r -> PersonKind.valueOf(r.get(PERSON.KIND)), r -> r.get(countField));
    }


    public Set<Person> findByIds(Set<Long> ids) {
        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.ID.in(ids))
                .fetchSet(personMapper);
    }


    public Set<Person> findByEmployeeIds(Set<String> empIds) {

        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.EMPLOYEE_ID.in(empIds)
                        .and(PERSON.IS_REMOVED.isFalse()))
                .fetchSet(personMapper);
    }


    public Set<Person> findActivePeopleByEmails(Set<String> emails) {
        return dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(PERSON.EMAIL.in(emails)
                        .and(PERSON.IS_REMOVED.isFalse()))
                .fetchSet(personMapper);
    }


    public Set<Person> findActivePeopleByUserRole(String role) {
        return dsl
                .select(PERSON.fields())
                .from(USER_ROLE)
                .innerJoin(PERSON).on(PERSON.EMAIL.eq(USER_ROLE.USER_NAME)
                        .and(PERSON.IS_REMOVED.isFalse()))
                .where(USER_ROLE.ROLE.eq(role))
                .fetchSet(personMapper);
    }


    public Set<Person> findDirectsForPersonIds(List<Long> personIds) {
        Condition cond = PERSON.MANAGER_EMPLOYEE_ID.in(DSL
                            .select(PERSON.EMPLOYEE_ID)
                            .from(PERSON)
                            .where(PERSON.ID.in(personIds)))
                    .andNot(PERSON.IS_REMOVED);

        SelectSeekStep1<Record, String> qry = dsl
                .select(PERSON.fields())
                .from(PERSON)
                .where(dsl.renderInlined(cond))
                .orderBy(PERSON.DISPLAY_NAME);

        return qry
                .fetchSet(personMapper);
    }
}
